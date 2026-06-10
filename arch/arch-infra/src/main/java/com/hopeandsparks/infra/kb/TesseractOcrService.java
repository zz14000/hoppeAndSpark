package com.hopeandsparks.infra.kb;

import com.hopeandsparks.infra.config.KbProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TesseractOcrService implements OcrService {

    private final KbProperties.Ocr properties;

    public TesseractOcrService(KbProperties.Ocr properties) {
        this.properties = properties;
    }

    @Override
    public String extract(Path path) {
        try {
            Path outputBase = Files.createTempFile("kb-ocr-", "");
            Files.deleteIfExists(outputBase);
            Process process = new ProcessBuilder(
                    properties.getCommand(),
                    path.toString(),
                    outputBase.toString(),
                    "-l",
                    properties.getLanguage()
            ).redirectErrorStream(true).start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return "";
            }
            Path txt = Path.of(outputBase.toString() + ".txt");
            if (!Files.exists(txt)) {
                return "";
            }
            return Files.readString(txt, StandardCharsets.UTF_8);
        } catch (IOException | InterruptedException exception) {
            Thread.currentThread().interrupt();
            return "";
        }
    }
}
