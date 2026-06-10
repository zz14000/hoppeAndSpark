package com.hopeandsparks.infra.kb;

import com.hopeandsparks.infra.file.FileStorageService;
import com.hopeandsparks.infra.file.StoredFileObject;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class RoutingDocumentParser implements DocumentParser {

    private static final Pattern HEADING_PATTERN = Pattern.compile("^(#{1,6}|第[一二三四五六七八九十0-9]+[章节部分]|[0-9]+(\\.[0-9]+)*|[一二三四五六七八九十]+、).+");

    private final FileStorageService fileStorageService;
    private final OcrService ocrService;

    public RoutingDocumentParser(FileStorageService fileStorageService, OcrService ocrService) {
        this.fileStorageService = fileStorageService;
        this.ocrService = ocrService;
    }

    @Override
    public ParsedDocument parse(DocumentParseRequest request) {
        return switch (request.sourceType()) {
            case TEXT -> fromText(request.title(), request.contentText(), "text/plain", Map.of("sourceType", "text"));
            case URL -> parseUrl(request);
            case VIDEO_URL -> parseVideoUrl(request);
            case FILE, IMAGE -> parseFile(request);
        };
    }

    private ParsedDocument parseUrl(DocumentParseRequest request) {
        try {
            org.jsoup.nodes.Document html = Jsoup.connect(request.sourceUrl()).get();
            String title = blankToDefault(html.title(), request.title());
            String text = normalize(html.body() == null ? html.text() : html.body().text());
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("sourceType", "url");
            metadata.put("sourceUrl", request.sourceUrl());
            return fromText(title, text, "text/html", metadata);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to fetch URL content: " + exception.getMessage(), exception);
        }
    }

    private ParsedDocument parseVideoUrl(DocumentParseRequest request) {
        String title = blankToDefault(request.title(), request.sourceUrl());
        String text = normalize(blankToDefault(request.contentText(),
                "Video URL: " + blankToDefault(request.sourceUrl(), "") + "\nPending transcript ingestion."));
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("sourceType", "video_url");
        metadata.put("sourceUrl", request.sourceUrl());
        metadata.put("transcriptStatus", "pending");
        return fromText(title, text, "text/plain", metadata);
    }

    private ParsedDocument parseFile(DocumentParseRequest request) {
        Optional<StoredFileObject> fileOptional = fileStorageService.open(request.fileId());
        if (fileOptional.isEmpty()) {
            throw new IllegalStateException("File not found for fileId=" + request.fileId());
        }
        StoredFileObject file = fileOptional.get();
        String fileName = file.file().fileName();
        String extension = extension(fileName);
        try {
            Path tempFile = Files.createTempFile("kb-parse-", "-" + fileName);
            try (InputStream inputStream = file.inputStream()) {
                Files.copy(inputStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            ParsedDocument parsed = switch (extension) {
                case "txt", "md", "csv", "json", "java", "js", "ts", "py", "xml", "yaml", "yml" ->
                        fromText(request.title(), Files.readString(tempFile, StandardCharsets.UTF_8), file.file().fileType(), Map.of(
                                "sourceType", "file",
                                "fileId", request.fileId(),
                                "fileName", fileName
                        ));
                case "pdf" -> parsePdf(request.title(), tempFile, fileName, request.fileId());
                case "docx" -> parseDocx(request.title(), tempFile, fileName, request.fileId());
                case "pptx" -> parsePptx(request.title(), tempFile, fileName, request.fileId());
                case "png", "jpg", "jpeg", "webp", "bmp", "tiff" -> parseImage(request.title(), tempFile, fileName, request.fileId());
                default -> fromText(request.title(), "Unsupported file type, preserved as raw file reference: " + fileName,
                        file.file().fileType(), Map.of("sourceType", "file", "fileId", request.fileId(), "fileName", fileName));
            };
            Files.deleteIfExists(tempFile);
            return parsed;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse uploaded file: " + exception.getMessage(), exception);
        }
    }

    private ParsedDocument parsePdf(String title, Path path, String fileName, String fileId) {
        try (org.apache.pdfbox.pdmodel.PDDocument document = Loader.loadPDF(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = normalize(stripper.getText(document));
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("sourceType", "file");
            metadata.put("fileId", fileId);
            metadata.put("fileName", fileName);
            metadata.put("pageCount", document.getNumberOfPages());
            metadata.put("ocrApplied", false);
            metadata.put("ocrRequired", text.isBlank());
            metadata.put("ocrSourceType", "pdf");
            return fromText(blankToDefault(title, fileName), text, "application/pdf",
                    metadata);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse PDF: " + exception.getMessage(), exception);
        }
    }

    private ParsedDocument parseDocx(String title, Path path, String fileName, String fileId) {
        try (InputStream inputStream = Files.newInputStream(path); XWPFDocument document = new XWPFDocument(inputStream)) {
            List<ParsedSection> sections = new ArrayList<>();
            StringBuilder content = new StringBuilder();
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            String currentTitle = blankToDefault(title, fileName);
            StringBuilder current = new StringBuilder();
            for (XWPFParagraph paragraph : paragraphs) {
                String text = normalize(paragraph.getText());
                if (text.isBlank()) {
                    continue;
                }
                if (isHeading(text)) {
                    flushSection(sections, currentTitle, current, sections.size() + 1);
                    currentTitle = text;
                    continue;
                }
                current.append(text).append('\n');
                content.append(text).append('\n');
            }
            flushSection(sections, currentTitle, current, sections.size() + 1);
            return new ParsedDocument(blankToDefault(title, fileName), normalize(content.toString()),
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document", sections,
                    Map.of("sourceType", "file", "fileId", fileId, "fileName", fileName));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse DOCX: " + exception.getMessage(), exception);
        }
    }

    private ParsedDocument parsePptx(String title, Path path, String fileName, String fileId) {
        try (InputStream inputStream = Files.newInputStream(path); XMLSlideShow slideShow = new XMLSlideShow(inputStream)) {
            List<ParsedSection> sections = new ArrayList<>();
            StringBuilder content = new StringBuilder();
            int index = 1;
            for (org.apache.poi.xslf.usermodel.XSLFSlide slide : slideShow.getSlides()) {
                String text = normalize(slide.getShapes().stream()
                        .map(shape -> {
                            if (shape instanceof org.apache.poi.xslf.usermodel.XSLFTextShape textShape) {
                                return textShape.getText();
                            }
                            return "";
                        })
                        .filter(value -> value != null && !value.isBlank())
                        .reduce("", (left, right) -> left + "\n" + right));
                if (text.isBlank()) {
                    index++;
                    continue;
                }
                String sectionTitle = "Slide " + index;
                sections.add(new ParsedSection(sectionTitle, 1, sectionTitle, text));
                content.append(sectionTitle).append('\n').append(text).append('\n');
                index++;
            }
            return new ParsedDocument(blankToDefault(title, fileName), normalize(content.toString()),
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation", sections,
                    Map.of("sourceType", "file", "fileId", fileId, "fileName", fileName));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse PPTX: " + exception.getMessage(), exception);
        }
    }

    private ParsedDocument parseImage(String title, Path path, String fileName, String fileId) {
        String ocrText = normalize(ocrService.extract(path));
        String text = ocrText.isBlank() ? "Image asset: " + fileName : ocrText;
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("sourceType", "image");
        metadata.put("fileId", fileId);
        metadata.put("fileName", fileName);
        metadata.put("ocrApplied", !ocrText.isBlank());
        metadata.put("ocrRequired", true);
        metadata.put("ocrSourceType", "image");
        return fromText(blankToDefault(title, fileName), text, "image/*", metadata);
    }

    private ParsedDocument fromText(String title, String text, String mediaType, Map<String, Object> metadata) {
        String normalized = normalize(text);
        List<ParsedSection> sections = splitSections(normalized, blankToDefault(title, "Untitled"));
        Map<String, Object> enriched = new LinkedHashMap<>();
        enriched.put("ocrApplied", false);
        enriched.put("ocrRequired", false);
        enriched.put("ocrSourceType", "none");
        if (metadata != null) {
            enriched.putAll(metadata);
        }
        return new ParsedDocument(blankToDefault(title, "Untitled"), normalized, mediaType, sections, enriched);
    }

    private List<ParsedSection> splitSections(String text, String fallbackTitle) {
        List<ParsedSection> sections = new ArrayList<>();
        if (text == null || text.isBlank()) {
            sections.add(new ParsedSection(fallbackTitle, 1, fallbackTitle, ""));
            return sections;
        }
        String[] lines = text.split("\\R");
        String currentTitle = fallbackTitle;
        StringBuilder currentContent = new StringBuilder();
        int index = 1;
        for (String rawLine : lines) {
            String line = normalize(rawLine);
            if (line.isBlank()) {
                continue;
            }
            if (isHeading(line) && currentContent.length() > 0) {
                sections.add(new ParsedSection(index + "." + currentTitle, 1, currentTitle, normalize(currentContent.toString())));
                currentTitle = line;
                currentContent.setLength(0);
                index++;
                continue;
            }
            currentContent.append(line).append('\n');
        }
        sections.add(new ParsedSection(index + "." + currentTitle, 1, currentTitle, normalize(currentContent.toString())));
        return sections;
    }

    private void flushSection(List<ParsedSection> sections, String title, StringBuilder content, int index) {
        String body = normalize(content.toString());
        if (!body.isBlank()) {
            sections.add(new ParsedSection(index + "." + title, 1, title, body));
        }
        content.setLength(0);
    }

    private boolean isHeading(String text) {
        return HEADING_PATTERN.matcher(text).matches() || text.length() <= 32 && !text.contains("。") && !text.contains(".");
    }

    private String extension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r", "\n").replaceAll("\n{3,}", "\n\n").trim();
    }
}
