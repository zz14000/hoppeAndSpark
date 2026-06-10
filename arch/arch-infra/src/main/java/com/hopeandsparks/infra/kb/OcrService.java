package com.hopeandsparks.infra.kb;

import java.nio.file.Path;

public interface OcrService {

    String extract(Path path);
}
