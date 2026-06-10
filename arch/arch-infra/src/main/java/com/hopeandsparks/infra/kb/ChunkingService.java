package com.hopeandsparks.infra.kb;

public interface ChunkingService {

    ChunkedDocument chunk(ParsedDocument document);
}
