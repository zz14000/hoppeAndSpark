package com.hopeandsparks.common.response;


/**
 * 文件职责：PageResponse 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\common\response\PageResponse.java，用于承载对应分层或接口的基础职责。
 */
import java.util.List;

public record PageResponse<T>(long page, long pageSize, long total, List<T> list) {
}

