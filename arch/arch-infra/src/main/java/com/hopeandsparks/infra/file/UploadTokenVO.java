package com.hopeandsparks.infra.file;

import java.time.LocalDateTime;

public record UploadTokenVO(
        String fileId,
        String uploadUrl,
        String objectKey,
        LocalDateTime expiresAt,
        boolean mock
) {
}
