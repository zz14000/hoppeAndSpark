package com.hopeandsparks.infra.chroma;

public record UpsertResult(ChromaScope scope, int count, boolean mock) {
}
