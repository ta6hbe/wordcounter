package com.hperperidis.wordcounter.services;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.hperperidis.wordcounter.exceptions.WordCounterException;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service Class providing functionality to assist in downloading file content via HTTP requests, in order to
 * be processed by the word counter service.
 *
 * Uses Non Blocking IO and returns futures. The call(s) are handled in an ASYNC manner, to ensure caller is
 * not blocked in long wait states while the HTTP request is completing.
 *
 * @author C. Perperidis(ta6hbe@hotmail.com)
 */
@Service
public class FileService {

    private final Path baseFileStore;

    public FileService() {
        try {
            UUID random = UUID.randomUUID();
            baseFileStore = Files.createTempDirectory("http_buffer_files_" + random.toString());
            baseFileStore.toFile().deleteOnExit();
        } catch (IOException exception) {
            throw new WordCounterException("Failed to create base file storage directory.", exception);
        }
    }

    public FileService(String path) {
        try {
            baseFileStore = Files.createDirectories(Paths.get(StringUtils.cleanPath(path)));
            baseFileStore.toFile().deleteOnExit();
        } catch (IOException exception) {
            throw new WordCounterException("Failed to create base file storage directory.", exception);
        }
    }

    /**
     * Creates a local temporary file in the base file store localtion, which is altogether marked for deletion
     * as soon as the jvm exists. (On app termination).
     *
     * Makes an HTTP GET connection to the provided URL, using NIO, and copies the contents of the URL to the file.
     * Returns a future, containing the file.
     *
     * On error will return a failed future.
     *
     * @param url {@link String} - The url to call to to download the file from.
     * @return {@link CompletableFuture<File>} - The file, containing the response content from the http call.*
     */
    public CompletableFuture<File> getFile(String url) {

        try {
            File data = File.createTempFile("httpget", "response", baseFileStore.toFile());

            try(FileOutputStream fileOutputStream = new FileOutputStream(data)) {
                ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(url).openStream());
                FileChannel fileChannel = fileOutputStream.getChannel();
                fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            }

            return CompletableFuture.completedFuture(data);
        } catch (IOException exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    /**
     * Creates a local temporary file in the base file store localtion, which is altogether marked for deletion
     * as soon as the jvm exists. (On app termination).
     *
     * Handles the Multipart file uploaded, using NIO, and copies the contents of the Multipart file to the local store.
     * Returns a future, containing the file.
     *
     * On error will return a failed future.
     *
     * @param file {@link MultipartFile} - The multipart file to read from.
     * @return {@link CompletableFuture<File>} - The local file, that represents the stored file from the MultipartFile request.
     *
     */
    public CompletableFuture<File> getMultiPartFile(MultipartFile file) {
        if (file == null || file.isEmpty() ) {
            return CompletableFuture.failedFuture(
                    new FileNotFoundException("File upload to local storage has failed. No file found."));
        }
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try(InputStream fileInputStream = file.getInputStream()) {
            Path localFilePath = this.baseFileStore.resolve(fileName);
            Files.copy(fileInputStream, localFilePath, StandardCopyOption.REPLACE_EXISTING);

            return CompletableFuture.completedFuture(localFilePath.toFile());
        } catch (IOException exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }
}
