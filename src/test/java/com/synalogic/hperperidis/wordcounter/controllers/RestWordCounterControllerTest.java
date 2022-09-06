package com.synalogic.hperperidis.wordcounter.controllers;

import java.io.File;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synalogic.hperperidis.wordcounter.http.WordCountRequest;
import com.synalogic.hperperidis.wordcounter.model.TextBook;
import com.synalogic.hperperidis.wordcounter.services.BookService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class RestWordCounterControllerTest {

    private static final String FIXTURE_TEST_PROBLEM_DEFINITION_STRING = "Hello world & good morning. The date is 18/05/2016";
    private static final String FIXTURE_TEST_APOSTROPHE = "thy brother's blood"; // from the bible

    private static final String FIXTURE_TEST_BIBLE_URL="https://janelwashere.com/files/bible_daily.txt";
    private static final String kingJamesBibleFileName = "kingJames.txt";

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    ObjectMapper objectMapper;

    @Mock
    BookService bookService;


    TextBook problemDefinitionTextBook;

    File kingJamesBible;

    @BeforeEach void setUp() {
        kingJamesBible = new File(this.getClass().getClassLoader().getResource(kingJamesBibleFileName).getFile());
        assertThat(kingJamesBible).isNotNull();
        assertThat(kingJamesBible.isFile()).isTrue();
        assertThat(kingJamesBible.isDirectory()).isFalse();

        problemDefinitionTextBook = TextBook.builder()
                .text(FIXTURE_TEST_PROBLEM_DEFINITION_STRING)
                .wordcount(9)
                .averageWordLength(4)
                .groupedCounts(Collections.singletonMap(4,4))
                .mostFrequentlyOccuringWordLength(Collections.singletonList(new AbstractMap.SimpleEntry<>(4,4)))
                .build();
    }

    @AfterEach void tearDown() {
    }

    @Test void countWordsFromString() throws Exception {

        WordCountRequest launchData = WordCountRequest.builder()
                .text(FIXTURE_TEST_PROBLEM_DEFINITION_STRING)
                .build();
        String payload = objectMapper.writeValueAsString(launchData);

        when(bookService.analyze(launchData))
                .thenReturn(CompletableFuture.completedFuture(problemDefinitionTextBook));

        MockHttpServletRequestBuilder request = post("/count/text")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload);

        MvcResult mvcResult = mockMvc.perform(request)
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.wordcount").exists())
                .andExpect(jsonPath("$.wordcount").value(9));

    }
}
