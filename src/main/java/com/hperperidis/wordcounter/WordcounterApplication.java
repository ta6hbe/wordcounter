package com.hperperidis.wordcounter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Application implementing a REST-Controller exposing two endpoints.

 */

@EnableCaching
@EnableWebMvc
@SpringBootApplication
@ServletComponentScan
public class WordcounterApplication {

	public static void main(String[] args) {
		SpringApplication.run(WordcounterApplication.class, args);
	}

}
