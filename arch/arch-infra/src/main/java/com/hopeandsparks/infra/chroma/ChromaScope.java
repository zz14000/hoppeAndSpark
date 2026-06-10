package com.hopeandsparks.infra.chroma;

public record ChromaScope(String userId, String projectId, String tenant, String database, String collection) {
}
