package com.hopeandsparks.infra.file;

import java.io.InputStream;

public record StoredFileObject(
        StoredFileVO file,
        InputStream inputStream,
        String objectKey
) {
}
