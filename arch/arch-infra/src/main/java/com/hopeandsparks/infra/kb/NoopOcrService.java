package com.hopeandsparks.infra.kb;

import java.nio.file.Path;

public class NoopOcrService implements OcrService {

    @Override
    public String extract(Path path) {
        return "";
    }
}
