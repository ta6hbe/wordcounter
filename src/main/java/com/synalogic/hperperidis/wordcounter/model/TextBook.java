package com.synalogic.hperperidis.wordcounter.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Simple POJO annotated with {@code com.fasterxml.jackson.annotation} annotations, to capture, serialise and de-serialise
 * a piece of text that is to be processed by the word counter logic in our {@code BookService}.
 *
 * When a request comes in, it is allocated to a {@code TextBook} bean, which contains the original text or the URL and
 * the downloaded text to be processed.
 *
 * The Bean is then passed to the service processor, which calculates the required text statistics and populates the necessary
 * fields.
 *
 * The bean is returned as the body of a {@code ResponseEntity}, where it is serialised again into the corresponding JSON text.
 *
 * @author C. Pereridis(ta6hbe@hotmail.com)
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TextBook {

    /**
     * The URL from where we can obtain the text to be processed.
     */
    @JsonProperty
    private String url;

    /**
     * The text to be processed.
     */
    @JsonProperty
    private String text;

    /**
     * Number of words in {@link this#text}
     */
    @JsonProperty
    private long wordcount;

    /**
     * The mean number of characters per word in the {@link this#text}.
     * {@see BookService} for a detailed explanation of how this is calculated by this service implementation.
     */
    @JsonProperty
    private float averageWordLength;

    /**
     * A {@code Map<Integer, Integer>}, keyed by the length of each word. So each word length has a single corresponding
     * entry in this map.
     * The corresponding value is the number of words that have the length (number of characters) denoted by the Key.
     *
     * So an {@code Map.Entry<>} of {3, 5} means the counter found 5 words of length 3 in the {@link this#text}.
     */
    @JsonProperty
    private Map<Integer, Integer> groupedCounts;

    /**
     * Any error message that may be thrown, or returned when processing a request that has this {@code TextBook} instance
     * returned.
     *
     * Field for serialising and communicating error messages in the API Response.
     */
    @JsonProperty
    private String errorMessage;

    /**
     * A {@code List} containing {@code Map.Entry<Integer, Integer>}. The pair(s) contained in this list are the
     * word lengths with the highest frequency counts in the {@link this#text}.
     *
     * E.g. A list containing entry pairs [{3, 6}, {5, 6}], means the {@link this#text}:
     * - contained 6 words of length 3
     * - also contained 6 words of length 5
     * - So word lengths of 3 and 5 characters where the most frequently occurring word lengths in the {@link this#text}.
     */
    @JsonProperty
    private List<Map.Entry<Integer, Integer>> mostFrequentlyOccuringWordLength;
}
