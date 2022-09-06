package com.hperperidis.wordcounter.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.hperperidis.wordcounter.model.TextBook;
import com.hperperidis.wordcounter.http.WordCountRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service class containing the logic for processing incoming request data and producing the necessary
 * word count statistics from the text retrieved, either directly from the incoming POST request, or from
 * downloading it from the URL provided in the POST request.
 *
 * This uses the following list of delimiter characters to split the text into words.
 * {@code {}[]()¬!@*+-_=|~\^<>.?;:"~# }
 * Notice the last character in the string above is white space.
 *
 * TODO: This could be further improved, by turning this to a configurable application option,
 *  so that we can pass the characters we want to use as delimiters via an environment launch com.synalogic.hperperidis.wordcounter.config,
 *  to try to alter the counter behaviour; e.g. if we want to process other languages, other than English!
 *  Another way this could work would be to create a map of language -> set of delimiters and
 *  accept either a language header or a language code in the incoming request.
 *
 * @author C. Perperidis(ta6hbe@hotmail.com)
 */

@Slf4j
@Service
public class BookService {
    private static final String DELIMITERS = "(){}[]¬!*+-_=|~\\^<>.?;:\"~ ";

    private final FileService fileService;

    @Autowired
    public BookService(FileService httpHelper) {
        this.fileService = httpHelper;
    }

    /**
     * This method receives the incoming POST request desirialised in a {@code WordCountRequest} model,
     * containing either the text to be processed, or a URL from which we have to download the relevant text,
     * before we can process it.
     *
     * Uses an {@code HttpHelper} instance to be able to download the corresponding text from the passed in URL.
     *
     * If no URL or no text are provided in the incoming request, this will generate a {@code ResponseStatusException}
     * with the appropriate response code. These will be automatically handled by Spring Framework's
     * {@code HandlerExceptionResolver}.
     *
     * @param request - {@code WordCountRequest} - The deserialised bean representing the received POST request data
     *                by the REST controller. This should either contain a URL or a populated text field.
     * @return - A populated {@code CompletableFuture<TextBook>}, containing the provided / retrieved text
     *           and its count statistics.
     */
    public CompletableFuture<TextBook> analyze(WordCountRequest request) {
        return parseRequest(request)
                .thenApply(this::processTextBook)
                .exceptionally(exception -> {
                    throw new ResponseStatusException(
                            HttpStatus.UNPROCESSABLE_ENTITY,
                            String.format("Failed to process request with error: %s", exception.getMessage()),
                            exception);
                });
    }

    /**
     * This method receives the incoming POST request desirialised in a {@code WordCountRequest} model,
     * containing either the text to be processed, or a URL from which we have to download the relevant text,
     * before we can process it.
     *
     * Uses an {@code HttpHelper} instance to be able to download the corresponding text from the passed in URL.
     *
     * If no URL or no text are provided in the incoming request, this will generate a {@code ResponseStatusException}
     * with the appropriate response code. These will be automatically handled by Spring Framework's
     * {@code HandlerExceptionResolver}.
     *
     * @param file - {@code MultipartFile} - The File uploaded during the request.
     *
     * @return - A populated {@code CompletableFuture<TextBook>}, containing the text contents of the Multipart file.
     *
     */
    public CompletableFuture<TextBook> analyze(MultipartFile file) {
         return textBookFromMultiPart(file)
                 .thenApply(this::processTextBook)
                 .exceptionally(exception -> {
                     throw new ResponseStatusException(
                             HttpStatus.UNPROCESSABLE_ENTITY,
                             String.format("Failed to process request with error: %s", exception.getMessage()),
                             exception);
                 });
    }

    /*
     * Creates a new Textbook and populates its text data from the retrieved file from remote URL.
     */
    private CompletableFuture<TextBook> texBookFromUrl(String url) {

        return fileService.getFile(url) // store url contents as a file in local storage
                .thenCompose((file) -> {
                    CompletableFuture<TextBook> textBook = fileToTextBook(file);
                    return textBook;
                }) // copy file contents to textbook return textbook
                .thenApply((textBook) -> {
                    TextBook textBook1 = textBook.toBuilder().url(url).build();

                    return textBook1;

                }) // add thes URL to the textbook
                .exceptionally(exception -> {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            String.format(
                                    "Failed to retrieve file from URL: [ %s ] with error: %s",
                                    url,
                                    exception.getMessage()),
                            exception);
                });
    }

    /*
     * Creates a new Textbook and populates its text data from the Multipart file uploaded.
     */
    private CompletableFuture<TextBook> textBookFromMultiPart(MultipartFile file) {

        return fileService.getMultiPartFile(file) // store the file to local storage
                .thenCompose(this::fileToTextBook) // copy file contents to textbook return textbook
                .exceptionally(exception -> {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            String.format(
                                    "Failed to retrieve uploaded file with error: %s",
                                    exception.getMessage()),
                            exception);
                });
    }

    /*
     * Parses a request bean to a relevant Textbook before processing.
     */
    private CompletableFuture<TextBook> parseRequest (WordCountRequest request) {
        if (StringUtils.isNotEmpty(request.getText())) {
            return CompletableFuture.completedFuture(TextBook.builder().text(request.getText()).build());
        }

        if (StringUtils.isNotEmpty(request.getUrl())) {
            return this.texBookFromUrl(request.getUrl());
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Cannot process this request. No text or URL to process provided");
    }

    private CompletableFuture<TextBook> fileToTextBook(File file) {
        final StringBuffer stringBuffer = new StringBuffer();
        try {
            Files.lines(file.toPath()).collect(Collectors.toList()).forEach(line -> {
                stringBuffer.append(line).append(" ");
            });
            return CompletableFuture.completedFuture(TextBook.builder().text(stringBuffer.toString()).build());
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }



    /*
     * Processes the text field of the passed in textbook.
     * It produces the word count statistics.
     */
    private TextBook processTextBook(TextBook textBook) {
        // Using treeMap for talies to ensure we get the keys in sorted order.
        // Immediately reveals highest and lowest word lengths and related frequencies.
        final TreeMap<Integer, Integer> countTally = new TreeMap<>();

        Arrays.stream(StringUtils.split(textBook.getText().trim(), DELIMITERS)).forEach((word) ->{
            if (countTally.get(word.length()) == null || !countTally.containsKey(word.length())) {
                countTally.put(word.length(), 1);
            } else {
                countTally.put(word.length(), countTally.get(word.length()) + 1);
            }
        });

        final int maxWordcount = Collections.max(countTally.values());
        textBook.setGroupedCounts(countTally);
        textBook.setWordcount(countTally.values().stream().mapToInt(Integer::intValue).sum());
        textBook.setAverageWordLength((float) countTally.entrySet().stream()
                .map(entry -> entry.getKey() * entry.getValue())
                .mapToInt(Integer::intValue)
                .sum() / textBook.getWordcount());
        textBook.setMostFrequentlyOccuringWordLength(
                countTally.entrySet().stream().filter(entry -> entry.getValue() == maxWordcount).collect(Collectors.toList()));

        return textBook;
    }
}
