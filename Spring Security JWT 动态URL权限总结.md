# Spring Security、JWT、URL 级动态权限总结

本文只总结本项目中和安全认证、JWT、URL 级动态权限有关的实现，方便迁移到其他项目。前端菜单和按钮权限不在本文范围内。

## 1. 总体架构

本项目安全体系分两层：

1. `mall-gateway` 网关层：做粗粒度认证，判断请求是否在白名单内、是否携带 JWT、Redis 中是否存在该用户 token。
2. 业务服务层：主要是 `mall-admin-ums` 依赖公共模块 `mall-security`，做 Spring Security 认证和 URL 级动态授权。

核心调用链：

```text
客户端请求
  -> mall-gateway/AuthGlobalFilter
     -> 白名单放行，或校验 Authorization 中的 JWT
     -> 根据用户名检查 Redis token key 是否存在
  -> 具体后台服务
     -> JwtAuthenticationTokenFilter
        -> 解析 JWT 得到 username
        -> UserDetailsService.loadUserByUsername(username)
        -> 查询用户和该用户拥有的 API 资源
        -> 封装 Authentication 放入 SecurityContext
     -> DynamicSecurityFilter
        -> DynamicSecurityMetadataSource 根据当前 URL 找接口所需资源
        -> DynamicAccessDecisionManager 比对用户拥有的资源
     -> Controller
```

涉及模块：

| 模块 | 作用 |
| --- | --- |
| `mall-security` | Spring Security 公共配置、JWT 工具、JWT 过滤器、动态权限过滤器、Redis 工具 |
| `mall-admin-ums/ums-server` | 后台用户、角色、资源、用户权限加载、资源动态映射 |
| `mall-gateway` | 网关全局认证过滤器，做 token 第一层拦截 |

## 2. Maven 依赖

后台服务通过依赖 `mall-security` 接入安全能力：

```xml
<dependency>
    <groupId>com.mtcarpenter</groupId>
    <artifactId>mall-security</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

`mall-security` 内部依赖：

```xml
<dependency>
    <artifactId>mall-common</artifactId>
    <groupId>com.mtcarpenter</groupId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
</dependency>
```

本项目版本为 Spring Boot `2.1.13.RELEASE`，Spring Security 仍使用 `WebSecurityConfigurerAdapter`。迁移到 Spring Boot 2.7/3.x 时需要改成 `SecurityFilterChain` 写法。

## 3. 配置项

后台服务和网关都需要保持 JWT、Redis key 前缀一致。

```yaml
jwt:
  tokenHeader: Authorization
  secret: mall-admin-secret
  expiration: 604800
  tokenHead: Bearer

secure:
  ignored:
    urls:
      - /swagger-ui.html
      - /swagger-resources/**
      - /swagger/**
      - /**/v2/api-docs
      - /**/*.js
      - /**/*.css
      - /**/*.png
      - /**/*.ico
      - /webjars/springfox-swagger-ui/**
      - /actuator/**
      - /druid/**
      - /admin/login
      - /admin/register

redis:
  database: mall
  key:
    admin: 'ums:admin'
    token: 'ums:token'
    resourceList: 'ums:resourceList'
  expire:
    common: 86400
```

含义：

| 配置 | 含义 |
| --- | --- |
| `jwt.tokenHeader` | 从哪个请求头读取 token，本项目为 `Authorization` |
| `jwt.tokenHead` | token 前缀，本项目返回给前端的是 `Bearer`，前端实际请求头为 `Bearer{token}` |
| `jwt.secret` | JWT 签名密钥，网关和后台服务必须一致 |
| `jwt.expiration` | JWT 过期时间，单位秒 |
| `secure.ignored.urls` | 白名单，网关和业务服务都会使用 |
| `redis.key.token` | 登录 token 存 Redis 的 key 前缀 |
| `redis.key.resourceList` | 用户 API 资源列表缓存 key 前缀 |

注意：`mall-admin-ums/ums-server/src/main/resources/application-dev.yml` 里当前缺少 `redis.key.token`，生产配置 `docs/nacos/mall-admin-ums-prod.yaml` 有该项。迁移时要补齐，否则 `@Value("${redis.key.token}")` 会注入失败。

## 4. Spring Security 公共配置

核心类：`mall-security/src/main/java/com/mtcarpenter/mall/security/config/SecurityConfig.java`

它做了几件事：

1. 读取 `secure.ignored.urls` 白名单，对这些 URL `permitAll()`。
2. 放行所有 `OPTIONS` 预检请求。
3. 其他请求必须认证：`anyRequest().authenticated()`。
4. 关闭 CSRF，使用无状态 Session：`SessionCreationPolicy.STATELESS`。
5. 配置未登录和无权限的 JSON 返回处理器。
6. 在 `UsernamePasswordAuthenticationFilter` 前加入 `JwtAuthenticationTokenFilter`。
7. 如果业务服务提供了 `dynamicSecurityService` Bean，则在 `FilterSecurityInterceptor` 前加入 `DynamicSecurityFilter`。

关键思想：

```text
SecurityConfig 是公共骨架
业务服务只需要继承它，并提供：
  1. UserDetailsService
  2. DynamicSecurityService
```

UMS 中的接入类是 `mall-admin-ums/ums-server/src/main/java/com/mtcarpenter/mall/config/MallSecurityConfig.java`：

```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MallSecurityConfig extends SecurityConfig {

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        return username -> adminService.loadUserByUsername(username);
    }

    @Bean
    public DynamicSecurityService dynamicSecurityService() {
        return () -> {
            Map<String, ConfigAttribute> map = new ConcurrentHashMap<>();
            List<UmsResource> resourceList = resourceService.listAll();
            for (UmsResource resource : resourceList) {
                map.put(
                    resource.getUrl(),
                    new org.springframework.security.access.SecurityConfig(
                        resource.getId() + ":" + resource.getName()
                    )
                );
            }
            return map;
        };
    }
}
```

迁移时，其他业务服务也可以复用这个模式：继承公共 `SecurityConfig`，实现自己的 `UserDetailsService` 和 `DynamicSecurityService`。

## 5. JWT 登录和认证

核心类：

| 类 | 作用 |
| --- | --- |
| `JwtTokenUtil` | 生成、解析、校验、刷新 JWT |
| `JwtAuthenticationTokenFilter` | 每次请求从请求头取 token，解析用户并放入 Spring Security 上下文 |
| `UmsAdminServiceImpl.login` | 登录校验、生成 token、保存 Redis |
| `UmsAdminController.login` | 登录接口，返回 `token` 和 `tokenHead` |

### 5.1 登录流程

`UmsAdminController.login` 调用：

```text
POST /admin/login
  -> adminService.login(username, password)
     -> loadUserByUsername(username)
        -> 查询 UmsAdmin
        -> 查询该管理员拥有的 UmsResource
        -> new AdminUserDetails(admin, resourceList)
     -> passwordEncoder.matches(rawPassword, encodedPassword)
     -> SecurityContextHolder.setAuthentication(...)
     -> jwtTokenUtil.generateToken(userDetails)
     -> adminCacheService.setToken(username, tokenHead + token)
     -> 记录登录日志
  -> 返回 { token, tokenHead }
```

JWT payload 中主要放两个字段：

```text
sub     -> username
created -> 创建时间
exp     -> 过期时间，由 jjwt 根据 setExpiration 写入
```

生成算法：

```java
Jwts.builder()
    .setClaims(claims)
    .setExpiration(generateExpirationDate())
    .signWith(SignatureAlgorithm.HS512, secret)
    .compact();
```

### 5.2 每次请求认证流程

`JwtAuthenticationTokenFilter` 继承 `OncePerRequestFilter`，每个请求执行一次：

```text
1. 从 Authorization 读取请求头
2. 判断是否以 tokenHead 开头
3. 截掉 tokenHead 得到真正 JWT
4. 解析 username
5. 调用 UserDetailsService.loadUserByUsername(username)
6. 校验 username 是否一致、token 是否过期
7. 创建 UsernamePasswordAuthenticationToken
8. 放入 SecurityContextHolder
```

放入 `SecurityContextHolder` 的意义：

1. 标记当前请求已经认证。
2. 后续动态权限过滤器可以从 `Authentication.getAuthorities()` 取当前用户权限。
3. Controller 或 Service 中可以通过 `Principal` 或 `SecurityContextHolder` 获取当前用户。

### 5.3 token 刷新

接口：`GET /admin/refreshToken`

流程：

```text
1. 从 Authorization 读取旧 token
2. jwtTokenUtil.refreshHeadToken(oldToken)
3. 如果旧 token 过期或解析失败，返回失败并删除 Redis token
4. 如果 30 分钟内刚刷新过，返回原 token
5. 否则更新 created，生成新 token
6. 新 token 写入 Redis
```

## 6. URL 级动态权限映射

本项目 URL 级权限不是写死在注解里，而是放在数据库资源表中。

核心表关系：

```text
ums_admin
  -> ums_admin_role_relation
  -> ums_role
  -> ums_role_resource_relation
  -> ums_resource
```

`ums_resource` 对应后端 API 资源，关键字段：

| 字段 | 含义 |
| --- | --- |
| `id` | 资源 ID |
| `name` | 资源名称 |
| `url` | API URL，支持 Ant 风格匹配，例如 `/product/**` |
| `category_id` | 资源分类 |

权限标识不是单独的字符串字段，而是拼出来的：

```text
资源权限标识 = resource.id + ":" + resource.name
例如：1:商品添加
```

这个字符串必须在两个地方保持一致：

1. 接口 URL 所需权限：`DynamicSecurityService.loadDataSource()` 中生成。
2. 当前用户拥有权限：`AdminUserDetails.getAuthorities()` 中生成。

## 7. 动态权限执行流程

核心类：

| 类 | 作用 |
| --- | --- |
| `DynamicSecurityService` | 业务服务实现，用于加载所有 URL -> 权限标识映射 |
| `DynamicSecurityMetadataSource` | 根据当前请求 URL 找到该 URL 需要哪些权限 |
| `DynamicSecurityFilter` | 接入 Spring Security 拦截链，触发动态授权 |
| `DynamicAccessDecisionManager` | 比对用户权限和接口所需权限，决定放行或拒绝 |

### 7.1 加载 URL -> 权限映射

UMS 的 `MallSecurityConfig.dynamicSecurityService()` 会查询所有资源：

```text
resourceService.listAll()
  -> ums_resource 全量列表
  -> map.put(resource.url, resource.id + ":" + resource.name)
```

得到类似：

```text
/product/create -> 1:商品添加
/product/update/** -> 2:商品修改
/order/** -> 10:订单管理
```

`DynamicSecurityMetadataSource` 在启动后 `@PostConstruct` 加载这份 Map。资源新增、修改、删除时，`UmsResourceController` 会调用：

```java
dynamicSecurityMetadataSource.clearDataSource();
```

下次请求时重新加载，达到动态刷新 URL 权限映射的效果。

### 7.2 请求 URL 匹配所需权限

`DynamicSecurityMetadataSource.getAttributes()`：

```text
1. 取当前请求 URL
2. 用 URLUtil.getPath(url) 去掉 query string
3. 遍历所有资源 URL pattern
4. 用 AntPathMatcher.match(pattern, path) 匹配
5. 匹配到的资源权限加入 configAttributes
6. 返回该请求所需权限集合
```

如果某个接口没有配置成 `ums_resource`，则返回空权限集合。

当前项目的 `DynamicAccessDecisionManager` 对空集合直接放行：

```java
if (CollUtil.isEmpty(configAttributes)) {
    return;
}
```

迁移时要特别注意：这意味着“未配置资源的接口默认允许已认证用户访问”。如果你的项目要求默认拒绝，应该改成抛 `AccessDeniedException`，或者强制所有需要保护的接口都录入资源表。

### 7.3 用户拥有权限加载

`UmsAdminServiceImpl.loadUserByUsername()`：

```text
1. 根据 username 查询管理员 UmsAdmin
2. 根据 adminId 查询资源列表 getResourceList(adminId)
3. new AdminUserDetails(admin, resourceList)
```

`getResourceList(adminId)` 先查 Redis：

```text
key = mall:ums:resourceList:{adminId}
```

缓存未命中再查数据库：

```sql
SELECT ur.*
FROM ums_admin_role_relation ar
LEFT JOIN ums_role r ON ar.role_id = r.id
LEFT JOIN ums_role_resource_relation rrr ON r.id = rrr.role_id
LEFT JOIN ums_resource ur ON ur.id = rrr.resource_id
WHERE ar.admin_id = #{adminId}
  AND ur.id IS NOT NULL
GROUP BY ur.id
```

`AdminUserDetails.getAuthorities()` 把资源列表转成 Spring Security 权限：

```java
return resourceList.stream()
    .map(resource -> new SimpleGrantedAuthority(resource.getId() + ":" + resource.getName()))
    .collect(Collectors.toList());
```

### 7.4 权限决策

`DynamicAccessDecisionManager.decide()`：

```text
1. 如果接口没有配置资源权限，直接放行
2. 遍历接口所需权限
3. 遍历当前用户拥有的 GrantedAuthority
4. 字符串完全相等则放行
5. 全部不匹配则抛 AccessDeniedException
```

本项目实际比较的是：

```text
needAuthority == grantedAuthority.getAuthority()
```

例如：

```text
接口 /product/create 需要：1:商品添加
用户权限列表包含：1:商品添加
结果：放行
```

## 8. Redis 在安全体系中的作用

本项目 Redis 主要存三类安全相关数据：

| Key 格式 | 数据 | 作用 |
| --- | --- | --- |
| `mall:ums:admin:{username}` | 管理员信息 | 减少用户信息查询 |
| `mall:ums:resourceList:{adminId}` | 用户拥有的 API 资源列表 | 减少每次认证时的权限查询 |
| `mall:ums:token:{username}` | 当前登录 token | 支持服务端维护登录状态、刷新 token、网关拦截 |

缓存失效场景：

| 操作 | 清理 |
| --- | --- |
| 修改/删除管理员 | 删除 `admin` 缓存 |
| 给用户分配角色 | 删除该用户 `resourceList` 缓存 |
| 角色分配资源 | 删除拥有该角色的用户 `resourceList` 缓存 |
| 修改/删除资源 | 删除拥有该资源的用户 `resourceList` 缓存，同时清空动态 URL 映射 |
| token 刷新失败 | 删除用户 token |

## 9. 网关层 token 校验

核心类：`mall-gateway/src/main/java/com/mtcarpenter/mall/filter/AuthGlobalFilter.java`

流程：

```text
1. OPTIONS 请求直接放行
2. 匹配 secure.ignored.urls 白名单，命中则放行
3. 从 Authorization 请求头读取 token
4. token 为空则抛 UNAUTHORIZED
5. 解析 username
6. 拼 Redis key：mall:ums:token:{username}
7. Redis 中不存在 token 则抛 UNAUTHORIZED
8. 存在则放行到后端服务
```

网关只负责第一层认证，不做 URL 资源权限判断。真正的 URL 级授权在业务服务的 `DynamicSecurityFilter` 中完成。

迁移时建议补强一点：当前网关只判断 Redis 中 token 是否存在，没有比较请求 token 和 Redis token 是否相等。更稳的写法是：

```text
resultToken != null && resultToken.equals(requestToken)
```

这样可以支持“同账号新登录踢掉旧 token”等场景。

## 10. 异常返回

两个处理器都在 `mall-security`：

| 类 | 场景 | 返回 |
| --- | --- | --- |
| `RestAuthenticationEntryPoint` | 未登录、token 无效、认证失败 | `CommonResult.unauthorized(...)` |
| `RestfulAccessDeniedHandler` | 已登录但无权限 | `CommonResult.forbidden(...)` |

它们都直接写 JSON 响应，并设置：

```text
Access-Control-Allow-Origin: *
Cache-Control: no-cache
Content-Type: application/json
```

## 11. 迁移到其他项目的步骤

1. 拷贝或重建公共安全模块：
   - `SecurityConfig`
   - `JwtTokenUtil`
   - `JwtAuthenticationTokenFilter`
   - `DynamicSecurityService`
   - `DynamicSecurityMetadataSource`
   - `DynamicSecurityFilter`
   - `DynamicAccessDecisionManager`
   - `RestAuthenticationEntryPoint`
   - `RestfulAccessDeniedHandler`
   - Redis 配置和 RedisService

2. 准备数据库表：
   - 用户表，例如 `sys_user`
   - 角色表，例如 `sys_role`
   - 用户角色关系表，例如 `sys_user_role`
   - API 资源表，例如 `sys_resource`
   - 角色资源关系表，例如 `sys_role_resource`

3. API 资源表至少要有：
   - `id`
   - `name`
   - `url`

4. 实现业务侧 `UserDetails`：
   - 包装用户信息。
   - `getAuthorities()` 返回该用户拥有的 API 资源权限。
   - 权限字符串必须和 `DynamicSecurityService` 中接口所需权限字符串一致。

5. 实现业务侧 `UserDetailsService`：
   - 根据用户名查用户。
   - 查询该用户通过角色拥有的 API 资源。
   - 返回自定义 `UserDetails`。

6. 实现业务侧 `DynamicSecurityService`：
   - 查询所有 API 资源。
   - 返回 `Map<urlPattern, ConfigAttribute>`。

7. 登录接口：
   - 校验用户名密码。
   - 生成 JWT。
   - 写 Redis：`{database}:{tokenPrefix}:{username}`。
   - 返回 `{ token, tokenHead }`。

8. 资源变更接口：
   - 新增、修改、删除 API 资源后调用 `dynamicSecurityMetadataSource.clearDataSource()`。
   - 角色资源关系变更后清理受影响用户的资源列表缓存。

9. 网关可选：
   - 如果是微服务项目，在网关做白名单和 token 第一层校验。
   - 单体项目可以不加网关，直接依赖业务服务中的 Spring Security 过滤器。

## 12. 迁移时要注意的坑

1. `tokenHead` 截取方式

   当前代码使用：

   ```java
   authHeader.substring(this.tokenHead.length())
   ```

   如果请求头是标准格式 `Bearer eyJ...`，截出来会带前导空格。当前项目的前端更像是拼成 `Bearer{token}`。迁移时建议统一成标准写法：

   ```java
   String prefix = tokenHead + " ";
   String authToken = authHeader.substring(prefix.length());
   ```

2. 未配置资源的接口默认放行

   当前 `DynamicAccessDecisionManager` 遇到空 `configAttributes` 直接 `return`。如果项目安全要求严格，应改成默认拒绝。

3. 权限标识不要依赖可变名称

   当前权限标识是 `id:name`。如果资源名称被修改，用户缓存里的权限字符串和动态资源映射可能短时间不一致。更稳的做法是只用不可变编码，例如 `RESOURCE_{id}` 或 `resource.code`。

4. 修改资源关系后要清缓存

   用户权限来自 Redis 缓存。角色资源关系、用户角色关系、资源本身变更后，必须清理对应 `resourceList` 缓存，否则旧权限会继续生效到缓存过期。

5. 网关和后台服务的 JWT 配置必须一致

   `jwt.secret`、`jwt.tokenHead`、`redis.database`、`redis.key.token` 不一致会导致网关解析通过不了，或查不到后台服务写入的 token。

6. 登出接口当前没有删除 Redis token

   当前 `UmsAdminController.logout()` 直接返回成功，没有调用 `delToken`。如果希望服务端登出立即失效，应在登出时解析当前用户名并删除 `mall:ums:token:{username}`。

## 13. 最小可迁移模型

如果只想在新项目中复用 URL 级动态权限，最小模型是：

```text
SecurityConfig
  -> 加 JWT 过滤器
  -> 加动态权限过滤器

JwtAuthenticationTokenFilter
  -> JWT -> username -> UserDetails -> Authentication

DynamicSecurityService
  -> DB 中所有 API 资源 -> Map<url, authority>

DynamicSecurityMetadataSource
  -> 当前请求 URL -> 需要的 authority

DynamicAccessDecisionManager
  -> 用户 authorities 是否包含接口 authority

UserDetailsService
  -> username -> user + resourceList

UserDetails
  -> resourceList -> GrantedAuthority
```

一句话概括：

```text
JWT 负责“你是谁”，UserDetails 负责“你拥有什么 API 资源”，DynamicSecurityMetadataSource 负责“当前 URL 需要什么资源”，DynamicAccessDecisionManager 负责“二者是否匹配”。
```
