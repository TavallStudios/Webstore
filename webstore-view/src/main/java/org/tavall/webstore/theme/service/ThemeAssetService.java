package org.tavall.webstore.theme.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tavall.webstore.config.StorefrontProperties;
import org.tavall.webstore.theme.view.ThemeFileView;
import org.tavall.webstore.theme.view.ThemeRenderView;

@Service
public class ThemeAssetService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("css", "js", "ts", "html", "txt", "md");
    private static final Set<String> RUNTIME_FILES = Set.of(
            "theme.css",
            "theme.js",
            "head.html",
            "body-top.html",
            "body-bottom.html",
            "home-top.html",
            "home-bottom.html",
            "product-top.html",
            "product-bottom.html",
            "footer.html"
    );
    private static final Map<String, String> DEFAULT_FILES = new LinkedHashMap<>();

    static {
        DEFAULT_FILES.put("theme.css", """
                /* Active theme stylesheet */
                :root {
                    --theme-panel-radius: 28px;
                }

                .theme-insert {
                    border-radius: var(--theme-panel-radius);
                }
                """);
        DEFAULT_FILES.put("theme.js", """
                document.documentElement.dataset.themeReady = "true";
                """);
        DEFAULT_FILES.put("head.html", """
                <!-- Optional extra meta tags, preload hints, or fonts -->
                """);
        DEFAULT_FILES.put("body-top.html", """
                <!-- Optional shared HTML injected after the header -->
                """);
        DEFAULT_FILES.put("body-bottom.html", """
                <!-- Optional shared HTML injected before the footer -->
                """);
        DEFAULT_FILES.put("home-top.html", """
                <!-- Optional HTML injected above the homepage story stack -->
                """);
        DEFAULT_FILES.put("home-bottom.html", """
                <!-- Optional HTML injected below the homepage story stack -->
                """);
        DEFAULT_FILES.put("product-top.html", """
                <!-- Optional HTML injected above the product layout -->
                """);
        DEFAULT_FILES.put("product-bottom.html", """
                <!-- Optional HTML injected below the product layout -->
                """);
        DEFAULT_FILES.put("footer.html", """
                <!-- Optional shared footer insert -->
                """);
    }

    private final Path rootPath;

    public ThemeAssetService(StorefrontProperties storefrontProperties) {
        this.rootPath = Paths.get(storefrontProperties.getTheme().getStoragePath()).toAbsolutePath().normalize();
        initializeWorkspace();
    }

    public List<ThemeFileView> listFiles() {
        initializeWorkspace();
        try (Stream<Path> files = Files.walk(rootPath)) {
            return files
                    .filter(Files::isRegularFile)
                    .map(rootPath::relativize)
                    .map(path -> toView(path.toString().replace('\\', '/')))
                    .sorted(Comparator
                            .comparing(ThemeFileView::runtimeFile).reversed()
                            .thenComparing(ThemeFileView::name))
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to list theme files.", exception);
        }
    }

    public String selectFile(String requestedFile) {
        String sanitized = sanitizeFileName(requestedFile);
        if (sanitized != null && Files.exists(resolvePath(sanitized))) {
            return sanitized;
        }
        return listFiles().stream()
                .map(ThemeFileView::name)
                .findFirst()
                .orElse("theme.css");
    }

    public String readFile(String fileName) {
        Path filePath = resolvePath(fileName);
        try {
            if (!Files.exists(filePath)) {
                return "";
            }
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read theme file: " + fileName, exception);
        }
    }

    public void saveFile(String fileName, String content) {
        String sanitized = sanitizeRequiredFileName(fileName);
        Path filePath = resolvePath(sanitized);
        try {
            Files.createDirectories(filePath.getParent());
            Files.writeString(
                    filePath,
                    content == null ? "" : content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save theme file: " + sanitized, exception);
        }
    }

    public void importFiles(MultipartFile[] files) {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String fileName = sanitizeRequiredFileName(file.getOriginalFilename());
            try {
                saveFile(fileName, new String(file.getBytes(), StandardCharsets.UTF_8));
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to import theme file: " + fileName, exception);
            }
        }
    }

    public ThemeRenderView buildRenderView() {
        initializeWorkspace();
        return new ThemeRenderView(
                Files.exists(resolvePath("theme.css")),
                Files.exists(resolvePath("theme.js")),
                readSnippet("head.html"),
                readSnippet("body-top.html"),
                readSnippet("body-bottom.html"),
                readSnippet("home-top.html"),
                readSnippet("home-bottom.html"),
                readSnippet("product-top.html"),
                readSnippet("product-bottom.html"),
                readSnippet("footer.html")
        );
    }

    public long countFiles() {
        return listFiles().size();
    }

    private void initializeWorkspace() {
        try {
            Files.createDirectories(rootPath);
            for (Map.Entry<String, String> entry : DEFAULT_FILES.entrySet()) {
                Path filePath = resolvePath(entry.getKey());
                if (!Files.exists(filePath)) {
                    Files.writeString(filePath, entry.getValue(), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to initialize the active theme workspace.", exception);
        }
    }

    private String readSnippet(String fileName) {
        Path filePath = resolvePath(fileName);
        if (!Files.exists(filePath)) {
            return "";
        }
        return readFile(fileName);
    }

    private ThemeFileView toView(String fileName) {
        Path filePath = resolvePath(fileName);
        try {
            String extension = extensionOf(fileName);
            return new ThemeFileView(
                    fileName,
                    extension,
                    Files.size(filePath),
                    Files.getLastModifiedTime(filePath).toInstant(),
                    RUNTIME_FILES.contains(fileName),
                    "/theme-assets/" + fileName
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to inspect theme file: " + fileName, exception);
        }
    }

    private Path resolvePath(String fileName) {
        Path resolvedPath = rootPath.resolve(fileName).normalize();
        if (!resolvedPath.startsWith(rootPath)) {
            throw new IllegalArgumentException("Theme file path is outside the active theme workspace.");
        }
        return resolvedPath;
    }

    private String sanitizeRequiredFileName(String fileName) {
        String sanitized = sanitizeFileName(fileName);
        if (sanitized == null) {
            throw new IllegalArgumentException("Unsupported theme file name.");
        }
        return sanitized;
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        try {
            String sanitized = Paths.get(fileName).normalize().toString().replace('\\', '/');
            if (sanitized.startsWith("../") || sanitized.startsWith("/") || sanitized.contains("..")) {
                return null;
            }
            String extension = extensionOf(sanitized);
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                return null;
            }
            return sanitized;
        } catch (InvalidPathException exception) {
            return null;
        }
    }

    private String extensionOf(String fileName) {
        int separatorIndex = fileName.lastIndexOf('.');
        if (separatorIndex < 0 || separatorIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(separatorIndex + 1).toLowerCase(Locale.ROOT);
    }
}
