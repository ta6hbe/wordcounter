package com.synalogic.hperperidis.wordcounter.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.springframework.web.multipart.MultipartFile;

/**
 * Simple POJO annotated with {@code com.fasterxml.jackson.annotation} annotations, to de-serialise incoming
 * request POST params from the request body.
 *
 * The service can be either called by passing the raw text to be processed in the body of the POST.
 * Alternatively it can be called via passing a URL from where the text to be processed will be downloaded,
 * before processing and returning the results.
 *
 * {@see RestWordCounterController} for more info in the incoming request details and how to make calls to
 * the service.
 *
 * @author C. Perperidis(ta6hbe@hotmail.com)
 *
 */

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WordCountRequest {

    @JsonProperty
    private String url;

    @JsonProperty
    private String text;

}
