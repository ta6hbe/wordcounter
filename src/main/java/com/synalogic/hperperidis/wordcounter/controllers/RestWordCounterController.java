package com.synalogic.hperperidis.wordcounter.controllers;

import java.util.concurrent.CompletableFuture;

import com.synalogic.hperperidis.wordcounter.http.WordCountRequest;
import com.synalogic.hperperidis.wordcounter.model.TextBook;
import com.synalogic.hperperidis.wordcounter.services.BookService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller to expose the `/count/text` endpoint.
 *
 * Simple Spring REST Controller delegating to a {@code BookService} to handle incoming Http requests
 *
 * Assuming the embedded Tomcat server will run locally on port 8080, example request for this can be made in the form of:
 *
 * - For Pulling text to process, from an online URL source:
 * {@code
 *   curl --location --request POST 'localhost:8080/count/text' \
 *      --header 'Content-Type: application/json' \
 *      --header 'Accept: application/json' \
 *      --data-raw '{"url": "https://janelwashere.com/files/bible_daily.txt"}'
 * }
 *
 * - For passing text to be processed as a POST body parameter:
 * {@code
 *      *   curl --location --request POST 'localhost:8080/count/text' \
 *      *      --header 'Content-Type: application/json' \
 *      *      --header 'Accept: application/json' \
 *      *      --data-raw '{"text": "Hello world & good morning. The date is 18/05/2016"}'
 *      * }
 *
 * @author C.Perperidis(ta6hbe@hotmail.com)
 */
@RestController
public class RestWordCounterController {

    private final BookService bookService;

    @Autowired
    public RestWordCounterController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     *
     * Post request end point.
     *
     * Handles incoming POST requests to end point URI `/count/text`
     * Accepts request data in the request body as {@code application/json} type and returns also {@code application/json}.
     *
     * @param request - the incoming request body decoded as {@code WordCountRequest} bean, containing
     *                either a URL or a text {@code String} to be processed.
     *
     * @return {@code CompletableFuture<ResponseEntity<TextBook>>} containing the processed text details.
     *
     */
    @PostMapping("/count/text")
    @ResponseBody
    public CompletableFuture<ResponseEntity<TextBook>> countWordsFromString(@RequestBody WordCountRequest request) {

        return bookService.analyze(request).thenApply(textBook -> new ResponseEntity<>(textBook, HttpStatus.OK));

    }

    @PostMapping("/count/file")
    public CompletableFuture<ResponseEntity<TextBook>> countWordsFromMultiPartFile(@RequestParam("file") MultipartFile file) {

        return bookService.analyze(file).thenApply(textBook -> new ResponseEntity<>(textBook, HttpStatus.OK));

    }

}
