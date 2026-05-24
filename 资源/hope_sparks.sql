/*
 Navicat Premium Dump SQL

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : hope_sparks

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 05/05/2026 12:49:15

 Discussion Revision: 2026/05/17
 1. Added sys_agent_config for Coze Bot / Workflow routing.
 2. Added Coze external mapping fields to agent_chat_session and agent_chat_message.
 3. Extended async_generation_task for Redis Stream/Coze workflow task tracking.
 4. Extended blog_post/blog_comment moderation status for MVP community review.
 5. Kept code question MVP on question_bank + user_question_record with AI judging fields.
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for agent_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `agent_chat_message`;
CREATE TABLE `agent_chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `sender_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'user/ai/system',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'text' COMMENT 'text/image/audio/file',
  `content_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文本内容',
  `content_file_id` bigint NULL DEFAULT NULL COMMENT '附件文件ID',
  `token_count` int NULL DEFAULT NULL COMMENT 'Token数',
  `external_message_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Coze消息ID',
  `raw_response` json NULL COMMENT 'Coze原始响应摘要',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_created`(`session_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_external_message_id`(`external_message_id` ASC) USING BTREE,
  INDEX `fk_message_file`(`content_file_id` ASC) USING BTREE,
  CONSTRAINT `fk_message_file` FOREIGN KEY (`content_file_id`) REFERENCES `sys_oss_file` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_message_session` FOREIGN KEY (`session_id`) REFERENCES `agent_chat_session` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '智能体消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for agent_chat_session
-- ----------------------------
DROP TABLE IF EXISTS `agent_chat_session`;
CREATE TABLE `agent_chat_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `agent_role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Ava/Sage/Coach/Strict/OldMoney/Nebula',
  `session_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '会话标题',
  `session_status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0关闭',
  `context_summary` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上下文摘要',
  `agent_config_id` bigint NULL DEFAULT NULL COMMENT '智能体运行配置ID',
  `external_conversation_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Coze会话ID',
  `external_section_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Coze Section ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_agent`(`user_id` ASC, `agent_role` ASC) USING BTREE,
  INDEX `idx_agent_config_id`(`agent_config_id` ASC) USING BTREE,
  INDEX `idx_external_conversation_id`(`external_conversation_id` ASC) USING BTREE,
  CONSTRAINT `fk_chat_session_agent_config` FOREIGN KEY (`agent_config_id`) REFERENCES `sys_agent_config` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_session_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '智能体会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for agent_graph_checkpoint
-- ----------------------------
DROP TABLE IF EXISTS `agent_graph_checkpoint`;
CREATE TABLE `agent_graph_checkpoint`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `thread_id` bigint NOT NULL COMMENT '线程ID',
  `checkpoint_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'LangGraph checkpoint标识',
  `parent_checkpoint_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '父checkpoint',
  `step_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图节点/步骤名',
  `state_json` json NOT NULL COMMENT '序列化状态',
  `message_count` int NOT NULL DEFAULT 0 COMMENT 'checkpoint时消息数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_checkpoint_id`(`checkpoint_id` ASC) USING BTREE,
  INDEX `idx_thread_created`(`thread_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `fk_checkpoint_thread` FOREIGN KEY (`thread_id`) REFERENCES `agent_graph_thread` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'LangGraph checkpoint持久化表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for agent_graph_thread
-- ----------------------------
DROP TABLE IF EXISTS `agent_graph_thread`;
CREATE TABLE `agent_graph_thread`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `session_id` bigint NULL DEFAULT NULL COMMENT '关联会话ID',
  `thread_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'LangGraph线程唯一标识',
  `graph_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图名称',
  `entry_agent` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '入口Agent',
  `thread_status` tinyint NOT NULL DEFAULT 1 COMMENT '1活跃 0关闭',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_thread_key`(`thread_key` ASC) USING BTREE,
  INDEX `idx_user_graph`(`user_id` ASC, `graph_name` ASC) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  CONSTRAINT `fk_graph_thread_session` FOREIGN KEY (`session_id`) REFERENCES `agent_chat_session` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_graph_thread_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'LangGraph线程表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for agent_memory
-- ----------------------------
DROP TABLE IF EXISTS `agent_memory`;
CREATE TABLE `agent_memory`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `thread_id` bigint NULL DEFAULT NULL COMMENT '线程ID，可为空',
  `session_id` bigint NULL DEFAULT NULL COMMENT '会话ID，可为空',
  `agent_role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Agent角色，公共记忆可为空',
  `memory_scope` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'public/user_private/agent_private/session_short',
  `memory_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'profile/preference/goal/fact/weakness/summary/strategy',
  `memory_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '结构化键',
  `memory_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '非结构化记忆文本',
  `memory_json` json NULL COMMENT '结构化记忆内容',
  `importance_score` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '重要度分',
  `confidence_score` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '可信度分',
  `source_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'chat' COMMENT 'chat/profile/eval/system/import',
  `source_ref_id` bigint NULL DEFAULT NULL COMMENT '来源记录ID',
  `valid_status` tinyint NOT NULL DEFAULT 1 COMMENT '1有效 0失效',
  `last_accessed_at` datetime NULL DEFAULT NULL COMMENT '最后访问时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_scope_type`(`user_id` ASC, `memory_scope` ASC, `memory_type` ASC) USING BTREE,
  INDEX `idx_thread_scope`(`thread_id` ASC, `memory_scope` ASC) USING BTREE,
  INDEX `idx_agent_scope`(`agent_role` ASC, `memory_scope` ASC) USING BTREE,
  INDEX `idx_importance_status`(`importance_score` ASC, `valid_status` ASC) USING BTREE,
  INDEX `fk_memory_session`(`session_id` ASC) USING BTREE,
  CONSTRAINT `fk_memory_session` FOREIGN KEY (`session_id`) REFERENCES `agent_chat_session` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_memory_thread` FOREIGN KEY (`thread_id`) REFERENCES `agent_graph_thread` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_memory_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '统一记忆表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for agent_memory_summary
-- ----------------------------
DROP TABLE IF EXISTS `agent_memory_summary`;
CREATE TABLE `agent_memory_summary`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `thread_id` bigint NULL DEFAULT NULL COMMENT '线程ID',
  `session_id` bigint NULL DEFAULT NULL COMMENT '会话ID',
  `agent_role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Agent角色',
  `summary_scope` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'session/thread/agent/user',
  `summary_stage` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'rolling/final/checkpoint',
  `summary_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '摘要文本',
  `covered_message_from` bigint NULL DEFAULT NULL COMMENT '覆盖消息起始ID',
  `covered_message_to` bigint NULL DEFAULT NULL COMMENT '覆盖消息结束ID',
  `token_count` int NULL DEFAULT NULL COMMENT '摘要Token数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_scope_stage`(`user_id` ASC, `summary_scope` ASC, `summary_stage` ASC) USING BTREE,
  INDEX `idx_thread_created`(`thread_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `fk_memory_summary_session`(`session_id` ASC) USING BTREE,
  CONSTRAINT `fk_memory_summary_session` FOREIGN KEY (`session_id`) REFERENCES `agent_chat_session` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_memory_summary_thread` FOREIGN KEY (`thread_id`) REFERENCES `agent_graph_thread` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_memory_summary_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '记忆滚动摘要表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for async_generation_task
-- ----------------------------
DROP TABLE IF EXISTS `async_generation_task`;
CREATE TABLE `async_generation_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `node_id` bigint NULL DEFAULT NULL COMMENT '知识点ID',
  `output_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'doc/video/audio/mindmap/report',
  `task_type` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'resource_generate' COMMENT 'resource_generate/resource_audit/plan_generate/kb_parse/kb_embed/community_moderation',
  `request_params` json NULL COMMENT '生成参数',
  `idempotent_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '幂等键',
  `external_run_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Coze工作流运行ID或第三方任务ID',
  `task_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT 'pending/processing/success/failed',
  `progress_percent` int NOT NULL DEFAULT 0 COMMENT '进度',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '当前重试次数',
  `max_retry` int NOT NULL DEFAULT 3 COMMENT '最大重试次数',
  `result_resource_id` bigint NULL DEFAULT NULL COMMENT '生成结果资源ID',
  `result_file_id` bigint NULL DEFAULT NULL COMMENT '生成结果文件ID',
  `error_msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `started_at` datetime NULL DEFAULT NULL COMMENT '任务开始时间',
  `finished_at` datetime NULL DEFAULT NULL COMMENT '任务结束时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_idempotent_key`(`idempotent_key` ASC) USING BTREE,
  INDEX `idx_user_status`(`user_id` ASC, `task_status` ASC) USING BTREE,
  INDEX `idx_task_type_status`(`task_type` ASC, `task_status` ASC) USING BTREE,
  INDEX `idx_external_run_id`(`external_run_id` ASC) USING BTREE,
  INDEX `idx_node_id`(`node_id` ASC) USING BTREE,
  INDEX `idx_result_resource_id`(`result_resource_id` ASC) USING BTREE,
  INDEX `idx_result_file_id`(`result_file_id` ASC) USING BTREE,
  CONSTRAINT `fk_async_node` FOREIGN KEY (`node_id`) REFERENCES `knowledge_node` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_async_result_file` FOREIGN KEY (`result_file_id`) REFERENCES `sys_oss_file` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_async_result_resource` FOREIGN KEY (`result_resource_id`) REFERENCES `learning_resource` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_async_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '异步生成任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for blog_comment
-- ----------------------------
DROP TABLE IF EXISTS `blog_comment`;
CREATE TABLE `blog_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父评论ID',
  `reply_to_user_id` bigint NULL DEFAULT NULL COMMENT '回复目标用户ID',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `comment_status` tinyint NOT NULL DEFAULT 2 COMMENT '0草稿 1已发布 2待审核 3风险待复核 4审核拦截 5管理员下架',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '缓存统计字段，以点赞事实表为准',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_parent`(`post_id` ASC, `parent_id` ASC) USING BTREE,
  INDEX `idx_comment_status_created`(`comment_status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_user_created`(`user_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `fk_blog_comment_parent`(`parent_id` ASC) USING BTREE,
  INDEX `fk_blog_comment_reply_user`(`reply_to_user_id` ASC) USING BTREE,
  CONSTRAINT `fk_blog_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `blog_comment` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_blog_comment_post` FOREIGN KEY (`post_id`) REFERENCES `blog_post` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_blog_comment_reply_user` FOREIGN KEY (`reply_to_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_blog_comment_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '博客评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for blog_favorite
-- ----------------------------
DROP TABLE IF EXISTS `blog_favorite`;
CREATE TABLE `blog_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '文章ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_post_favorite`(`user_id` ASC, `post_id` ASC) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  CONSTRAINT `fk_blog_favorite_post` FOREIGN KEY (`post_id`) REFERENCES `blog_post` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_blog_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '博客收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for blog_like
-- ----------------------------
DROP TABLE IF EXISTS `blog_like`;
CREATE TABLE `blog_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'post/comment',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target_like`(`user_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_target_type_id`(`target_type` ASC, `target_id` ASC) USING BTREE,
  CONSTRAINT `fk_blog_like_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '博客点赞事实表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for blog_post
-- ----------------------------
DROP TABLE IF EXISTS `blog_post`;
CREATE TABLE `blog_post`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '作者ID',
  `title` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章标题',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '摘要',
  `content_md` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Markdown正文',
  `cover_file_id` bigint NULL DEFAULT NULL COMMENT '封面文件ID',
  `post_status` tinyint NOT NULL DEFAULT 2 COMMENT '0草稿 1已发布 2待审核 3风险待复核 4审核拦截 5管理员下架',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '缓存统计字段，以浏览事实表为准',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '缓存统计字段，以点赞事实表为准',
  `favorite_count` int NOT NULL DEFAULT 0 COMMENT '缓存统计字段，以收藏事实表为准',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_created`(`user_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_post_status_created`(`post_status` ASC, `created_at` ASC) USING BTREE,
  INDEX `fk_blog_post_cover_file`(`cover_file_id` ASC) USING BTREE,
  CONSTRAINT `fk_blog_post_cover_file` FOREIGN KEY (`cover_file_id`) REFERENCES `sys_oss_file` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_blog_post_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '博客文章表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for blog_view_log
-- ----------------------------
DROP TABLE IF EXISTS `blog_view_log`;
CREATE TABLE `blog_view_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID，游客为空',
  `device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备ID',
  `view_date` date NOT NULL COMMENT '浏览日期',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_view_date`(`post_id` ASC, `view_date` ASC) USING BTREE,
  INDEX `idx_user_view_date`(`user_id` ASC, `view_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '博客浏览日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for challenge_submission
-- ----------------------------
DROP TABLE IF EXISTS `challenge_submission`;
CREATE TABLE `challenge_submission`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `challenge_id` bigint NOT NULL COMMENT '挑战ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `solution_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提交内容',
  `is_anonymous` tinyint NOT NULL DEFAULT 1 COMMENT '是否匿名',
  `ai_score` decimal(5, 2) NULL DEFAULT NULL COMMENT 'AI评分',
  `peer_votes` int NOT NULL DEFAULT 0 COMMENT '同侪投票数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_challenge_user`(`challenge_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_created`(`user_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `fk_submission_challenge` FOREIGN KEY (`challenge_id`) REFERENCES `weekly_challenge` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_submission_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '挑战提交表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程编码',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程名称',
  `course_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '课程简介',
  `major_domain` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所属专业',
  `difficulty_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'medium' COMMENT '课程难度',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_course_code`(`course_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for evaluation_report
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_report`;
CREATE TABLE `evaluation_report`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `course_id` bigint NULL DEFAULT NULL COMMENT '课程ID',
  `practice_set_id` bigint NULL DEFAULT NULL COMMENT '练习集ID',
  `overall_score` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '综合得分',
  `knowledge_score_json` json NULL COMMENT '知识点维度评分',
  `ability_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '能力总结',
  `improvement_suggestion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '改进建议',
  `generated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'Coach',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_course`(`user_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `idx_practice_set_id`(`practice_set_id` ASC) USING BTREE,
  INDEX `fk_eval_course`(`course_id` ASC) USING BTREE,
  CONSTRAINT `fk_eval_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_eval_practice_set` FOREIGN KEY (`practice_set_id`) REFERENCES `practice_set` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_eval_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评测报告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for feedback_ticket
-- ----------------------------
DROP TABLE IF EXISTS `feedback_ticket`;
CREATE TABLE `feedback_ticket`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '提交人',
  `target_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'resource/chat_message/comment/chunk/system',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `issue_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'hallucination/inappropriate/bug/other',
  `description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题描述',
  `snapshot_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '提交时快照',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT 'pending/reviewed/fixed/rejected',
  `admin_id` bigint NULL DEFAULT NULL COMMENT '处理管理员ID',
  `process_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_target_type_id`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_status_admin`(`status` ASC, `admin_id` ASC) USING BTREE,
  INDEX `fk_ticket_user`(`user_id` ASC) USING BTREE,
  INDEX `fk_ticket_admin`(`admin_id` ASC) USING BTREE,
  CONSTRAINT `fk_ticket_admin` FOREIGN KEY (`admin_id`) REFERENCES `sys_admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_ticket_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '争议工单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for im_conversation
-- ----------------------------
DROP TABLE IF EXISTS `im_conversation`;
CREATE TABLE `im_conversation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'private/group/system',
  `biz_id` bigint NULL DEFAULT NULL COMMENT '群聊时为group_id，系统通知可为空',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type_biz`(`conversation_type` ASC, `biz_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for im_message
-- ----------------------------
DROP TABLE IF EXISTS `im_message`;
CREATE TABLE `im_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'text' COMMENT 'text/image/file/system',
  `content_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息文本',
  `content_file_id` bigint NULL DEFAULT NULL COMMENT '附件文件ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conversation_created`(`conversation_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_sender_created`(`sender_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `fk_im_message_file`(`content_file_id` ASC) USING BTREE,
  CONSTRAINT `fk_im_message_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `im_conversation` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_im_message_file` FOREIGN KEY (`content_file_id`) REFERENCES `sys_oss_file` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_im_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for im_message_read
-- ----------------------------
DROP TABLE IF EXISTS `im_message_read`;
CREATE TABLE `im_message_read`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message_id` bigint NOT NULL COMMENT '消息ID',
  `user_id` bigint NOT NULL COMMENT '已读用户ID',
  `read_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_user_read`(`message_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_read_at`(`user_id` ASC, `read_at` ASC) USING BTREE,
  CONSTRAINT `fk_im_read_message` FOREIGN KEY (`message_id`) REFERENCES `im_message` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_im_read_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息已读回执表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_chunk_record
-- ----------------------------
DROP TABLE IF EXISTS `kb_chunk_record`;
CREATE TABLE `kb_chunk_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `chunk_index` int NOT NULL COMMENT '切片序号',
  `content_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '切片内容',
  `token_size` int NOT NULL COMMENT '切片Token数',
  `chroma_point_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '向量点ID',
  `embed_status` tinyint NOT NULL DEFAULT 0 COMMENT '0未完成 1已完成 2失败',
  `is_active` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_document_chunk_index`(`document_id` ASC, `chunk_index` ASC) USING BTREE,
  UNIQUE INDEX `uk_chroma_point_id`(`chroma_point_id` ASC) USING BTREE,
  INDEX `idx_document_embed_status`(`document_id` ASC, `embed_status` ASC) USING BTREE,
  CONSTRAINT `fk_chunk_document` FOREIGN KEY (`document_id`) REFERENCES `kb_document` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识切片表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_document
-- ----------------------------
DROP TABLE IF EXISTS `kb_document`;
CREATE TABLE `kb_document`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `kb_domain` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所属领域',
  `course_id` bigint NULL DEFAULT NULL COMMENT '关联课程ID',
  `node_id` bigint NULL DEFAULT NULL COMMENT '关联知识点ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文档标题',
  `file_id` bigint NOT NULL COMMENT '文件ID',
  `doc_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'pdf/word/md/code',
  `source_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'official' COMMENT 'official/user/uploaded',
  `collection_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Chroma集合名',
  `parse_strategy_id` bigint NULL DEFAULT NULL COMMENT '解析策略ID',
  `embedding_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '向量模型',
  `embedding_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '向量版本',
  `document_version` int NOT NULL DEFAULT 1 COMMENT '文档版本',
  `total_tokens` int NOT NULL DEFAULT 0 COMMENT '总Token数',
  `chunk_count` int NOT NULL DEFAULT 0 COMMENT '切片数',
  `parse_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT 'pending/parsing/embedding/success/failed',
  `uploader_id` bigint NOT NULL COMMENT '上传者ID',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_domain_status`(`kb_domain` ASC, `parse_status` ASC) USING BTREE,
  INDEX `idx_course_node`(`course_id` ASC, `node_id` ASC) USING BTREE,
  INDEX `idx_file_id`(`file_id` ASC) USING BTREE,
  INDEX `idx_collection_name`(`collection_name` ASC) USING BTREE,
  INDEX `fk_kb_node`(`node_id` ASC) USING BTREE,
  INDEX `fk_kb_strategy`(`parse_strategy_id` ASC) USING BTREE,
  CONSTRAINT `fk_kb_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_kb_file` FOREIGN KEY (`file_id`) REFERENCES `sys_oss_file` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_kb_node` FOREIGN KEY (`node_id`) REFERENCES `knowledge_node` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_kb_strategy` FOREIGN KEY (`parse_strategy_id`) REFERENCES `kb_parse_strategy` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识库文档表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for kb_parse_strategy
-- ----------------------------
DROP TABLE IF EXISTS `kb_parse_strategy`;
CREATE TABLE `kb_parse_strategy`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `strategy_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '策略名称',
  `chunk_size` int NOT NULL DEFAULT 500 COMMENT '切片大小',
  `chunk_overlap` int NOT NULL DEFAULT 50 COMMENT '切片重叠',
  `separators` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '切分符',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '解析策略表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for knowledge_node
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_node`;
CREATE TABLE `knowledge_node`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父节点ID',
  `node_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '知识点编码',
  `node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '知识点名称',
  `node_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '知识点说明',
  `difficulty_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'medium' COMMENT '难度',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_course_node_code`(`course_id` ASC, `node_code` ASC) USING BTREE,
  INDEX `idx_course_parent`(`course_id` ASC, `parent_id` ASC) USING BTREE,
  CONSTRAINT `fk_node_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识点表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for knowledge_node_relation
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_node_relation`;
CREATE TABLE `knowledge_node_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `source_node_id` bigint NOT NULL COMMENT '起始知识点',
  `target_node_id` bigint NOT NULL COMMENT '目标知识点',
  `relation_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'pre/next/related/dependency',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_node_relation`(`source_node_id` ASC, `target_node_id` ASC, `relation_type` ASC) USING BTREE,
  INDEX `idx_target_node`(`target_node_id` ASC) USING BTREE,
  CONSTRAINT `fk_relation_source_node` FOREIGN KEY (`source_node_id`) REFERENCES `knowledge_node` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_relation_target_node` FOREIGN KEY (`target_node_id`) REFERENCES `knowledge_node` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识点关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for learning_resource
-- ----------------------------
DROP TABLE IF EXISTS `learning_resource`;
CREATE TABLE `learning_resource`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `node_id` bigint NOT NULL COMMENT '知识点ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源标题',
  `resource_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'doc/video/mindmap/quiz/code/ppt',
  `resource_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'medium' COMMENT '适配难度',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '摘要',
  `content_source_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'file' COMMENT 'file/text/url',
  `current_file_id` bigint NULL DEFAULT NULL COMMENT '当前版本文件ID，冗余缓存字段',
  `generated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'Nebula' COMMENT '生成来源',
  `generate_status` tinyint NOT NULL DEFAULT 1 COMMENT '1有效 0失效',
  `horizon_check_status` tinyint NOT NULL DEFAULT 0 COMMENT '0未核查 1通过 2驳回',
  `quality_score` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '质量评分',
  `current_version_no` int NOT NULL DEFAULT 1 COMMENT '当前生效版本号，冗余缓存字段',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_node_type`(`node_id` ASC, `resource_type` ASC) USING BTREE,
  INDEX `idx_current_file_id`(`current_file_id` ASC) USING BTREE,
  CONSTRAINT `fk_resource_current_file` FOREIGN KEY (`current_file_id`) REFERENCES `sys_oss_file` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_resource_node` FOREIGN KEY (`node_id`) REFERENCES `knowledge_node` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for learning_resource_version
-- ----------------------------
DROP TABLE IF EXISTS `learning_resource_version`;
CREATE TABLE `learning_resource_version`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `version_no` int NOT NULL COMMENT '版本号',
  `content_file_id` bigint NULL DEFAULT NULL COMMENT '文件ID',
  `change_summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本说明',
  `horizon_check_status` tinyint NOT NULL DEFAULT 0 COMMENT '核查状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_resource_version`(`resource_id` ASC, `version_no` ASC) USING BTREE,
  INDEX `idx_version_file_id`(`content_file_id` ASC) USING BTREE,
  CONSTRAINT `fk_version_file` FOREIGN KEY (`content_file_id`) REFERENCES `sys_oss_file` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_version_resource` FOREIGN KEY (`resource_id`) REFERENCES `learning_resource` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习资源版本表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mall_item
-- ----------------------------
DROP TABLE IF EXISTS `mall_item`;
CREATE TABLE `mall_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `item_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `item_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'virtual/service',
  `cost_points` int NOT NULL COMMENT '消耗积分',
  `duration_days` int NULL DEFAULT NULL COMMENT '有效天数',
  `item_status` tinyint NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_item_status`(`item_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商城商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for practice_set
-- ----------------------------
DROP TABLE IF EXISTS `practice_set`;
CREATE TABLE `practice_set`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `set_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '练习集名称',
  `set_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'daily/mock/exam/review',
  `difficulty_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'medium',
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'Coach',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_type`(`course_id` ASC, `set_type` ASC) USING BTREE,
  CONSTRAINT `fk_practice_set_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '练习集表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for practice_set_question
-- ----------------------------
DROP TABLE IF EXISTS `practice_set_question`;
CREATE TABLE `practice_set_question`  (
  `practice_set_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `sort_order` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`practice_set_id`, `question_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE,
  CONSTRAINT `fk_psq_question` FOREIGN KEY (`question_id`) REFERENCES `question_bank` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_psq_set` FOREIGN KEY (`practice_set_id`) REFERENCES `practice_set` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '练习集题目关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for question_bank
-- ----------------------------
DROP TABLE IF EXISTS `question_bank`;
CREATE TABLE `question_bank`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `node_id` bigint NOT NULL COMMENT '知识点ID',
  `question_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'single/multi/fill/essay/code',
  `difficulty_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'medium',
  `content_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题干',
  `options_json` json NULL COMMENT '选项',
  `standard_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '标准答案',
  `analysis_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '题目解析',
  `source_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'generated' COMMENT 'generated/manual/imported',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_node_type_level`(`node_id` ASC, `question_type` ASC, `difficulty_level` ASC) USING BTREE,
  CONSTRAINT `fk_question_node` FOREIGN KEY (`node_id`) REFERENCES `knowledge_node` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '题库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for study_plan
-- ----------------------------
DROP TABLE IF EXISTS `study_plan`;
CREATE TABLE `study_plan`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `plan_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计划名称',
  `plan_status` tinyint NOT NULL DEFAULT 0 COMMENT '0待执行 1执行中 2已完成 3已暂停',
  `start_date` date NULL DEFAULT NULL COMMENT '计划开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '计划结束日期',
  `generated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'Strict' COMMENT '生成智能体',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_course`(`user_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `fk_plan_course`(`course_id` ASC) USING BTREE,
  CONSTRAINT `fk_plan_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_plan_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习计划表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for study_task
-- ----------------------------
DROP TABLE IF EXISTS `study_task`;
CREATE TABLE `study_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL COMMENT '计划ID',
  `node_id` bigint NULL DEFAULT NULL COMMENT '知识点ID',
  `resource_id` bigint NULL DEFAULT NULL COMMENT '资源ID',
  `task_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务标题',
  `task_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'learn/review/practice/exam',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '计划内排序',
  `plan_start_time` datetime NULL DEFAULT NULL COMMENT '计划开始时间',
  `plan_end_time` datetime NULL DEFAULT NULL COMMENT '计划结束时间',
  `task_status` tinyint NOT NULL DEFAULT 0 COMMENT '0待办 1进行中 2已完成 3逾期',
  `progress_percent` int NOT NULL DEFAULT 0 COMMENT '完成进度',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plan_status_time`(`plan_id` ASC, `task_status` ASC, `plan_start_time` ASC) USING BTREE,
  INDEX `idx_node_id`(`node_id` ASC) USING BTREE,
  INDEX `idx_resource_id`(`resource_id` ASC) USING BTREE,
  CONSTRAINT `fk_task_node` FOREIGN KEY (`node_id`) REFERENCES `knowledge_node` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_task_plan` FOREIGN KEY (`plan_id`) REFERENCES `study_plan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_task_resource` FOREIGN KEY (`resource_id`) REFERENCES `learning_resource` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_achievement_badge
-- ----------------------------
DROP TABLE IF EXISTS `sys_achievement_badge`;
CREATE TABLE `sys_achievement_badge`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `badge_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '徽章名称',
  `badge_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '徽章说明',
  `badge_icon_file_id` bigint NULL DEFAULT NULL COMMENT '徽章图标文件ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '徽章字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_admin
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '管理员账号',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '真实姓名',
  `password_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码哈希',
  `admin_status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admin_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_agent_prompt
-- ----------------------------
DROP TABLE IF EXISTS `sys_agent_prompt`;
CREATE TABLE `sys_agent_prompt`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `agent_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '智能体标识',
  `agent_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '智能体名称',
  `system_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '系统提示词',
  `temperature` decimal(3, 2) NULL DEFAULT 0.70,
  `max_tokens` int NULL DEFAULT 2048,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '最后修改人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_agent_key`(`agent_key` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '智能体提示词配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_agent_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_agent_config`;
CREATE TABLE `sys_agent_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `agent_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'nebula/horizon/strict/ava/sage/coach/oldmoney',
  `agent_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '智能体名称',
  `coze_space_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Coze空间ID',
  `coze_bot_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Coze Bot ID',
  `coze_workflow_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Coze Workflow ID',
  `default_model_provider` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备用模型供应商',
  `stream_enabled` tinyint NOT NULL DEFAULT 0 COMMENT '是否支持流式',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用',
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'v1' COMMENT '配置版本',
  `extra_config` json NULL COMMENT '超时、温度、参数映射等扩展配置',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_agent_code`(`agent_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '智能体运行配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_chat_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_chat_group`;
CREATE TABLE `sys_chat_group`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '群名称',
  `owner_id` bigint NOT NULL COMMENT '群主ID',
  `avatar_file_id` bigint NULL DEFAULT NULL COMMENT '群头像文件ID',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '群简介',
  `member_count` int NOT NULL DEFAULT 1 COMMENT '缓存统计字段，以群成员表为准',
  `group_status` tinyint NOT NULL DEFAULT 1 COMMENT '1正常 0停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  INDEX `fk_group_avatar_file`(`avatar_file_id` ASC) USING BTREE,
  CONSTRAINT `fk_group_avatar_file` FOREIGN KEY (`avatar_file_id`) REFERENCES `sys_oss_file` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_group_owner` FOREIGN KEY (`owner_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '群组表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_group_member
-- ----------------------------
DROP TABLE IF EXISTS `sys_group_member`;
CREATE TABLE `sys_group_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint NOT NULL COMMENT '群组ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `member_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'member' COMMENT 'owner/admin/member',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_user`(`group_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_group_member_group` FOREIGN KEY (`group_id`) REFERENCES `sys_chat_group` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_group_member_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '群成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NOT NULL COMMENT '管理员ID',
  `module_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块',
  `action_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作类型',
  `target_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标类型',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标ID',
  `detail` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作详情',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作IP',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin_module`(`admin_id` ASC, `module_name` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_oss_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_oss_file`;
CREATE TABLE `sys_oss_file`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件哈希',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件名',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'MIME类型',
  `bucket_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '桶名',
  `object_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '对象存储Key',
  `file_size` bigint NOT NULL COMMENT '文件大小',
  `duration_seconds` int NULL DEFAULT 0 COMMENT '音视频时长',
  `transcript_text` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '字幕或转写文本',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_file_hash`(`file_hash` ASC) USING BTREE,
  INDEX `idx_type_created`(`file_type` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件资产表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_admin_resource_category
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin_resource_category`;
CREATE TABLE `sys_admin_resource_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源分类名称',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admin_resource_category_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '后台资源分类表' ROW_FORMAT = Dynamic;

INSERT INTO `sys_admin_resource_category`
(`id`, `name`, `sort_order`, `status`)
VALUES
(1, '认证与会话', 10, 1),
(2, '数据看板', 20, 1),
(3, '用户治理', 30, 1),
(4, '知识库管理', 40, 1),
(5, '争议工单', 50, 1),
(6, '存储与资源治理', 60, 1),
(7, 'Agent 配置', 70, 1),
(8, 'AI 审核与风控', 80, 1),
(9, 'RBAC 与系统审计', 90, 1);

-- ----------------------------
-- Table structure for sys_admin_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin_menu`;
CREATE TABLE `sys_admin_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父目录ID，0表示根目录',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目录名称',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端路由或目录路径',
  `leval` int NOT NULL DEFAULT 1 COMMENT '树层级',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_sort`(`parent_id` ASC, `sort_order` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '后台目录表' ROW_FORMAT = Dynamic;

INSERT INTO `sys_admin_menu`
(`id`, `parent_id`, `name`, `path`, `leval`, `sort_order`, `status`)
VALUES
(1, 0, '数据看板', '/manage/dashboard', 1, 10, 1),
(2, 0, '用户治理', '/manage/users', 1, 20, 1),
(3, 0, '知识库管理', '/manage/knowledge-base', 1, 30, 1),
(4, 0, '争议工单', '/manage/ai-disputes', 1, 40, 1),
(5, 0, '资源治理', '/manage/resources', 1, 50, 1),
(6, 0, 'Agent 配置', '/manage/agent-prompts', 1, 60, 1),
(7, 0, 'AI 审核与风控', '/manage/moderation', 1, 70, 1),
(8, 0, '系统权限', '/manage/system', 1, 90, 1),
(9, 2, '用户列表', '/manage/users/list', 2, 10, 1),
(10, 3, '文档列表', '/manage/knowledge-base/documents', 2, 10, 1),
(11, 4, '工单列表', '/manage/ai-disputes/list', 2, 10, 1),
(12, 5, '存储监控', '/manage/storage/overview', 2, 10, 1),
(13, 5, '历史资源', '/manage/resources/history', 2, 20, 1),
(14, 6, 'Prompt 配置', '/manage/agent-prompts/list', 2, 10, 1),
(15, 7, '内容审核列表', '/manage/moderation/content', 2, 10, 1),
(16, 7, '行为预警列表', '/manage/moderation/behavior-alerts', 2, 20, 1),
(17, 8, '角色管理', '/manage/system/roles', 2, 10, 1),
(18, 8, '管理员管理', '/manage/system/admins', 2, 20, 1),
(19, 8, '操作日志', '/manage/system/operation-logs', 2, 30, 1);

-- ----------------------------
-- Table structure for sys_admin_resource
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin_resource`;
CREATE TABLE `sys_admin_resource`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` bigint NOT NULL COMMENT '后台资源分类ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源名称',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源标识',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Controller基础路径，取类上@RequestMapping的值',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资源说明',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admin_resource_code`(`code` ASC) USING BTREE,
  UNIQUE INDEX `uk_admin_resource_url`(`url` ASC) USING BTREE,
  INDEX `idx_admin_resource_category`(`category_id` ASC) USING BTREE,
  INDEX `idx_admin_resource_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_admin_resource_category` FOREIGN KEY (`category_id`) REFERENCES `sys_admin_resource_category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '后台Controller资源表' ROW_FORMAT = Dynamic;

INSERT INTO `sys_admin_resource`
(`id`, `category_id`, `name`, `code`, `url`, `description`, `sort_order`, `status`)
VALUES
(1, 2, '数据看板资源', 'manage:dashboard', '/api/v1/manage/dashboard', '后台数据看板统计', 10, 1),
(2, 3, '用户列表资源', 'manage:user:list', '/api/v1/manage/users', '用户列表、用户状态治理、学习轨迹查看', 20, 1),
(3, 4, '知识库文档列表资源', 'manage:kb:document', '/api/v1/manage/knowledge-base/documents', '知识库文档列表、上传、编辑、删除、解析状态', 30, 1),
(4, 5, '工单列表资源', 'manage:dispute:list', '/api/v1/manage/ai-disputes', 'AI争议工单列表、复核、处理', 40, 1),
(5, 6, '存储监控资源', 'manage:storage:overview', '/api/v1/manage/storage', '存储容量、资源统计、冗余资源监控', 50, 1),
(6, 6, '历史资源列表资源', 'manage:resource:history', '/api/v1/manage/resources', '历史生成资源列表、详情、删除', 60, 1),
(7, 7, 'Prompt 配置资源', 'manage:agent_prompt', '/api/v1/manage/agent-prompts', 'Agent Prompt 列表、编辑、启停', 70, 1),
(8, 8, '内容审核列表资源', 'manage:moderation:content', '/api/v1/manage/moderation/content', '内容审核列表、通过、阻断、下架', 80, 1),
(9, 8, '行为预警列表资源', 'manage:moderation:behavior_alert', '/api/v1/manage/moderation/behavior-alerts', '行为预警列表、标记、处置、备注', 90, 1),
(10, 9, '菜单管理资源', 'manage:system:menu', '/api/v1/manage/menus', '后台菜单树查询与后续菜单维护', 100, 1),
(11, 9, '角色管理资源', 'manage:system:role', '/api/v1/manage/roles', '角色列表、创建角色、编辑角色、分配菜单与资源', 110, 1),
(12, 9, '管理员管理资源', 'manage:system:admin', '/api/v1/manage/admins', '管理员列表、创建管理员、分配角色、禁用管理员', 120, 1),
(13, 9, '操作日志资源', 'manage:system:operation_log', '/api/v1/manage/operation-logs', '后台操作日志查询；日志写入不依赖该权限', 130, 1);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色标识',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_key`(`role_key` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_admin_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin_role`;
CREATE TABLE `sys_admin_role`  (
  `admin_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`admin_id`, `role_id`) USING BTREE,
  INDEX `fk_admin_role_role`(`role_id` ASC) USING BTREE,
  CONSTRAINT `fk_admin_role_admin` FOREIGN KEY (`admin_id`) REFERENCES `sys_admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_admin_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role_admin_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_admin_menu`;
CREATE TABLE `sys_role_admin_menu`  (
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE,
  INDEX `fk_role_admin_menu_menu`(`menu_id` ASC) USING BTREE,
  CONSTRAINT `fk_role_admin_menu_menu` FOREIGN KEY (`menu_id`) REFERENCES `sys_admin_menu` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_role_admin_menu_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色目录关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role_admin_resource
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_admin_resource`;
CREATE TABLE `sys_role_admin_resource`  (
  `role_id` bigint NOT NULL,
  `resource_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`, `resource_id`) USING BTREE,
  INDEX `fk_role_admin_resource_resource`(`resource_id` ASC) USING BTREE,
  CONSTRAINT `fk_role_admin_resource_resource` FOREIGN KEY (`resource_id`) REFERENCES `sys_admin_resource` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_role_admin_resource_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色后台Controller资源关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `password_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码哈希',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `account_status` tinyint NOT NULL DEFAULT 1 COMMENT '1正常 2封禁 3注销',
  `ban_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封禁原因',
  `ban_until` datetime NULL DEFAULT NULL COMMENT '封禁截止时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  INDEX `idx_status`(`account_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_achievement
-- ----------------------------
DROP TABLE IF EXISTS `user_achievement`;
CREATE TABLE `user_achievement`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `badge_id` bigint NOT NULL COMMENT '徽章ID',
  `obtained_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_badge`(`user_id` ASC, `badge_id` ASC) USING BTREE,
  INDEX `idx_badge_id`(`badge_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_achievement_badge` FOREIGN KEY (`badge_id`) REFERENCES `sys_achievement_badge` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_achievement_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户徽章表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_asset
-- ----------------------------
DROP TABLE IF EXISTS `user_asset`;
CREATE TABLE `user_asset`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `item_id` bigint NOT NULL COMMENT '商品ID',
  `asset_status` tinyint NOT NULL DEFAULT 1 COMMENT '1有效 0失效',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_expire`(`user_id` ASC, `expire_time` ASC) USING BTREE,
  INDEX `idx_item_id`(`item_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_asset_item` FOREIGN KEY (`item_id`) REFERENCES `mall_item` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_asset_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户资产表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_friend
-- ----------------------------
DROP TABLE IF EXISTS `user_friend`;
CREATE TABLE `user_friend`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `friend_user_id` bigint NOT NULL COMMENT '好友用户ID',
  `friend_status` tinyint NOT NULL DEFAULT 0 COMMENT '0申请中 1已通过 2已拉黑',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_friend`(`user_id` ASC, `friend_user_id` ASC) USING BTREE,
  INDEX `idx_friend_status`(`friend_user_id` ASC, `friend_status` ASC) USING BTREE,
  CONSTRAINT `fk_friend_target_user` FOREIGN KEY (`friend_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_friend_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '好友关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_knowledge_progress
-- ----------------------------
DROP TABLE IF EXISTS `user_knowledge_progress`;
CREATE TABLE `user_knowledge_progress`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `node_id` bigint NOT NULL,
  `progress_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'locked' COMMENT 'locked/learning/mastered/review',
  `progress_percent` int NOT NULL DEFAULT 0 COMMENT '进度百分比',
  `mastery_score` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '掌握度评分',
  `last_review_time` datetime NULL DEFAULT NULL COMMENT '最后复习时间',
  `review_due_time` datetime NULL DEFAULT NULL COMMENT '下次复习时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_node`(`user_id` ASC, `node_id` ASC) USING BTREE,
  INDEX `idx_node_status`(`node_id` ASC, `progress_status` ASC) USING BTREE,
  CONSTRAINT `fk_progress_node` FOREIGN KEY (`node_id`) REFERENCES `knowledge_node` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_progress_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户知识进度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_learning_record
-- ----------------------------
DROP TABLE IF EXISTS `user_learning_record`;
CREATE TABLE `user_learning_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `task_id` bigint NULL DEFAULT NULL COMMENT '任务ID',
  `resource_id` bigint NULL DEFAULT NULL COMMENT '资源ID',
  `record_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'view/play/read/practice/chat',
  `duration_seconds` int NOT NULL DEFAULT 0 COMMENT '学习时长秒数',
  `record_date` date NOT NULL COMMENT '学习日期',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_record_date`(`user_id` ASC, `record_date` ASC) USING BTREE,
  INDEX `idx_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_resource_id`(`resource_id` ASC) USING BTREE,
  CONSTRAINT `fk_learning_record_resource` FOREIGN KEY (`resource_id`) REFERENCES `learning_resource` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_learning_record_task` FOREIGN KEY (`task_id`) REFERENCES `study_task` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_learning_record_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户学习记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_login_session
-- ----------------------------
DROP TABLE IF EXISTS `user_login_session`;
CREATE TABLE `user_login_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `session_token` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录会话令牌标识',
  `device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备ID',
  `device_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备名称',
  `client_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'web/android/ios',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录IP',
  `last_active_at` datetime NULL DEFAULT NULL COMMENT '最后活跃时间',
  `expires_at` datetime NOT NULL COMMENT '过期时间',
  `session_status` tinyint NOT NULL DEFAULT 1 COMMENT '1有效 0失效',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_session_token`(`session_token` ASC) USING BTREE,
  INDEX `idx_user_status`(`user_id` ASC, `session_status` ASC) USING BTREE,
  CONSTRAINT `fk_session_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户登录会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_profile
-- ----------------------------
DROP TABLE IF EXISTS `user_profile`;
CREATE TABLE `user_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `major_domain` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '专业/领域',
  `grade_level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学段/年级',
  `knowledge_base_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'beginner' COMMENT '基础水平',
  `cognitive_style` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '认知风格',
  `learning_preference` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学习偏好',
  `error_preference` json NULL COMMENT '易错点偏好',
  `learning_goal` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学习目标',
  `self_discipline` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'B' COMMENT '自律等级',
  `current_weakness` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当前薄弱项',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_profile_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户动态画像表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_question_record
-- ----------------------------
DROP TABLE IF EXISTS `user_question_record`;
CREATE TABLE `user_question_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `practice_set_id` bigint NULL DEFAULT NULL COMMENT '练习集ID',
  `user_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '用户答案',
  `is_correct` tinyint NULL DEFAULT NULL COMMENT '是否正确',
  `score` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '得分',
  `judge_mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'auto' COMMENT 'auto/ai/manual',
  `feedback_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '反馈',
  `is_flagged` tinyint NOT NULL DEFAULT 0 COMMENT '是否加入错题本',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_question`(`user_id` ASC, `question_id` ASC) USING BTREE,
  INDEX `idx_set_id`(`practice_set_id` ASC) USING BTREE,
  INDEX `fk_record_question`(`question_id` ASC) USING BTREE,
  CONSTRAINT `fk_record_practice_set` FOREIGN KEY (`practice_set_id`) REFERENCES `practice_set` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_record_question` FOREIGN KEY (`question_id`) REFERENCES `question_bank` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_record_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户答题记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_resource_favorite
-- ----------------------------
DROP TABLE IF EXISTS `user_resource_favorite`;
CREATE TABLE `user_resource_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `resource_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_resource_fav`(`user_id` ASC, `resource_id` ASC) USING BTREE,
  INDEX `fk_fav_resource`(`resource_id` ASC) USING BTREE,
  CONSTRAINT `fk_fav_resource` FOREIGN KEY (`resource_id`) REFERENCES `learning_resource` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户资源收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_settings
-- ----------------------------
DROP TABLE IF EXISTS `user_settings`;
CREATE TABLE `user_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `enable_tts` tinyint NOT NULL DEFAULT 1 COMMENT 'TTS开关',
  `enable_ava_popup` tinyint NOT NULL DEFAULT 1 COMMENT 'Ava提醒开关',
  `enable_focus_mode` tinyint NOT NULL DEFAULT 1 COMMENT '专注模式',
  `public_collection` tinyint NOT NULL DEFAULT 1 COMMENT '是否公开收藏',
  `theme_mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'light' COMMENT '主题模式',
  `font_scale` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'normal' COMMENT '字体大小',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_settings_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_settings_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户设置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for weekly_challenge
-- ----------------------------
DROP TABLE IF EXISTS `weekly_challenge`;
CREATE TABLE `weekly_challenge`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '挑战标题',
  `course_id` bigint NULL DEFAULT NULL COMMENT '关联课程ID',
  `scene_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '场景说明',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `challenge_status` tinyint NOT NULL DEFAULT 1 COMMENT '1进行中 0关闭',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_time`(`course_id` ASC, `start_time` ASC) USING BTREE,
  CONSTRAINT `fk_challenge_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '周挑战表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
