package com.hopeandsparks.infra.kb;

public interface DocumentParser {

    ParsedDocument parse(DocumentParseRequest request);
}
