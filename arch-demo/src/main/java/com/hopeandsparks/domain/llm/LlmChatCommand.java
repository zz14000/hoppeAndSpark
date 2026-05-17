package com.hopeandsparks.domain.llm;


/**
 * 文件职责：LlmChatCommand 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\domain\llm\LlmChatCommand.java，用于承载对应分层或接口的基础职责。
 */
public record LlmChatCommand(String provider, String prompt) {
}

