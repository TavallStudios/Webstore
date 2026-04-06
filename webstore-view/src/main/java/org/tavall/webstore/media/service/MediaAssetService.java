package org.tavall.webstore.media.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tavall.webstore.config.StorefrontProperties;
import org.tavall.webstore.storefront.view.MediaAssetDescriptor;

@Service
public class MediaAssetService {

    private final Path mediaDirectory;

    public MediaAssetService(StorefrontProperties storefrontProperties) {
        this.mediaDirectory = Path.of(storefrontProperties.getMedia().getStoragePath()).toAbsolutePath().normalize();
    }

    public List<MediaAssetDescriptor> listAssets() {
        try {
            Files.createDirectories(mediaDirectory);
            return Files.list(mediaDirectory)
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::getFileName))
                    .map(path -> {
                        try {
                            return new MediaAssetDescriptor(
                                    path.getFileName().toString(),
                                    "/media/" + path.getFileName(),
                                    Files.size(path),
                                    Files.getLastModifiedTime(path).toInstant()
                            );
                        } catch (IOException exception) {
                            throw new IllegalStateException("Unable to read media asset metadata.", exception);
                        }
                    })
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to list media assets.", exception);
        }
    }

    public MediaAssetDescriptor store(MultipartFile multipartFile) {
        try {
            Files.createDirectories(mediaDirectory);
            String originalFileName = multipartFile.getOriginalFilename() == null ? "asset.bin" : multipartFile.getOriginalFilename();
            String sanitizedFileName = Instant.now().toEpochMilli() + "-" + originalFileName.replace(" ", "-");
            Path targetPath = mediaDirectory.resolve(sanitizedFileName);
            try (InputStream inputStream = multipartFile.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return new MediaAssetDescriptor(
                    sanitizedFileName,
                    "/media/" + sanitizedFileName,
                    multipartFile.getSize(),
                    Files.getLastModifiedTime(targetPath).toInstant()
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to store media asset.", exception);
        }
    }
}
