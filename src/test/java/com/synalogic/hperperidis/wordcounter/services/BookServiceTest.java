package com.synalogic.hperperidis.wordcounter.services;

import java.io.File;
import java.net.http.HttpConnectTimeoutException;
import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.synalogic.hperperidis.wordcounter.http.WordCountRequest;
import com.synalogic.hperperidis.wordcounter.model.TextBook;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    private static final String URL = "https://janelwashere.com/files/bible_daily.txt";
    private static final String BADURL = "https://example.com/no/books/to/be/found/here.txt";
    private static final String kingJamesBibleFileName = "kingJames.txt";

    private static final String FIXTURE_TEST_PROBLEM_DEFINITION_STRING = "Hello world & good morning. The date is 18/05/2016";
    private static final String FIXTURE_TEST_APOSTROPHE = "thy brother's blood"; // from the bible

    private WordCountRequest problemDefinitionRequest;
    private WordCountRequest checkApostropheRequest;
    private WordCountRequest kingJamesBibleUrlRequest;
    private WordCountRequest badUrlRequest;

    @InjectMocks
    private BookService bookService;

    @Mock
    private FileService httpHelper;

    File kingJamesBible;
    DecimalFormat df;

    @BeforeEach
    void setUp() {
        df = new DecimalFormat("#.###");

        problemDefinitionRequest = WordCountRequest.builder().text(FIXTURE_TEST_PROBLEM_DEFINITION_STRING).build();
        checkApostropheRequest = WordCountRequest.builder().text(FIXTURE_TEST_APOSTROPHE).build();
        kingJamesBibleUrlRequest = WordCountRequest.builder().url(URL).build();
        badUrlRequest = WordCountRequest.builder().url(BADURL).build();

        kingJamesBible = new File(this.getClass().getClassLoader().getResource(kingJamesBibleFileName).getFile());
        assertThat(kingJamesBible).isNotNull();
        assertThat(kingJamesBible.isFile()).isTrue();
        assertThat(kingJamesBible.isDirectory()).isFalse();
    }

    @AfterEach
    void tearDown() {
        bookService = null;
    }

    @Test
    void analyzeProblemDefinitionBookReturnsCorrectNumberOfWords() throws Exception {
        TextBook textBook = bookService.analyze(problemDefinitionRequest).get();
        assertThat(textBook.getWordcount()).isEqualTo(9);
        assertThat(textBook.getMostFrequentlyOccuringWordLength().size()).isEqualTo(2);
        assertThat(df.format(textBook.getAverageWordLength())).isEqualTo(df.format(4.556));

        textBook = bookService.analyze(checkApostropheRequest).get();
        assertThat(textBook.getWordcount()).isEqualTo(3);
        assertThat(textBook.getMostFrequentlyOccuringWordLength().size()).isEqualTo(3);
        assertThat(df.format(textBook.getAverageWordLength())).isEqualTo(df.format(5.667));
    }

    @Test
    void getBookFromURL() {

        when(httpHelper.getFile(eq(URL))).thenReturn(
                CompletableFuture.completedFuture(kingJamesBible));
        assertThatCode(() -> bookService.analyze(kingJamesBibleUrlRequest).get()).doesNotThrowAnyException();

        when(httpHelper.getFile(eq(BADURL)))
                .thenReturn(CompletableFuture.failedFuture(new HttpConnectTimeoutException("The connection timed out.")));

        assertThatCode(() -> bookService.analyze(badUrlRequest).get())
                .isInstanceOf(ExecutionException.class)
                .hasMessageContaining("The connection timed out");
    }

    @Test
    void getBookFromURLContainsCorrectTextAndCounts() throws Exception {

        when(httpHelper.getFile(eq(URL))).thenReturn(
                CompletableFuture.completedFuture(kingJamesBible));

        TextBook kingJamesBibleBook = bookService.analyze(kingJamesBibleUrlRequest).get();

        assertThat(kingJamesBibleBook.getText()).contains("King James Version of the Bible");
        assertThat(kingJamesBibleBook.getErrorMessage()).isNull();
        assertThat(kingJamesBibleBook.getAverageWordLength()).isNotNull().isGreaterThan(4).isLessThan(5);
        assertThat(kingJamesBibleBook.getWordcount()).isEqualTo(793145);
        assertThat(kingJamesBibleBook.getMostFrequentlyOccuringWordLength().size()).isEqualTo(1);
    }
}
