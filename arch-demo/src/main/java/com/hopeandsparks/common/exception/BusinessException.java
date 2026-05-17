package com.hopeandsparks.common.exception;


/**
 * 文件职责：BusinessException 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\common\exception\BusinessException.java，用于承载对应分层或接口的基础职责。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

