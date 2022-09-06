# Word Count Service APP:
### Author: C Perperidis
### email: ta6hbe@hotmail.com
### date: Tuesday 06/09/2022

## Intro:
This is a simple SpringBoot App, implementing a **Word Counting** service exposed via a 
REST API publishing two HTTP POST endpoints.
- /count/text
- /count/file

This can be downloaded from my Github repo [here](https://github.com/ta6hbe/wordcounter).

I have made an effort to make the endpoints and services as efficient as possible, by 
using NON Blocking IO for File and HTTP download operations.

The service and controller endpoints also return CompletableFuture(s) so that any processing
can be completed and dispatched asynchronously, without blocking the controller.

It uses maven and contains a `pom.xml` file. It builds a self-contained executable
jar which runs an embedded Tomcat server exposing port 8080 on the local host.

If you have any other process using port 8080, you will need to stop it, before
running this. You can also run this using Docker-Compose configuration provided, in which case 
you can customise the local host port you wish to bind to the launched container.

Future improvements may include (but not limited to):
- Expanding the endpoint request to contain locale information based on which to choose charset and delimiters.
- Adding further endpoints for other text processing functions... 

## Contents:

- [Intro](#intro)
- [API Documentation](#api) 
- [How it works](#how-it-works)
- [Word Definition](#definition-of-a-word)
- [How to launch ](#how-to-launch)
- [Conclusion](#conclusion)

---------------

# API:

### /count/text:

REST endpoint for receiving JSON POST requests with either a:
- {"url": "http://example.com"} - The url to attempt to download content from.
- {"text": "This is a text example of data that can be sent"} - Raw text data to be processed

#### E.g.

```
curl --location --request POST 'localhost:8080/count/text' \
--header 'Accept: application/json' \
--header 'Content-Type: application/json' \
--data-raw '{"url": "https://janelwashere.com/files/bible_daily.txt"}'
```

#### Or even:
```
curl --location --request POST 'localhost:8080/count/text' \
--header 'Accept: application/json' \
--header 'Content-Type: application/json' \
--data-raw '{"text": "Hello World. Today it is 06/09/2022 (at the time of writing ;-))"}'

```

### /count/file:
Multipart file upload endpoint for receiving a POST request with a multipart file upload.
E.g.
```
curl --location --request POST 'localhost:8080/count/file' \
--header 'Content-Type: multipart/form-data' \
--header 'Accept: application/json' \
--form 'file=@"/Users/hperperidis/Desktop/Keep/LoremIpsum.txt"'
```

#### Expected result:

The above should produce and HTTP-OK (200) response with a JSON serialised TextBook body like:
```
{
    "url": null,
    "text": "Hello World. Today it is 06/09/2022 (at the time of writing ;-))",
    "wordcount": 11,
    "averageWordLength": 4.2727275,
    "groupedCounts": {
        "2": 4,
        "3": 1,
        "4": 1,
        "5": 3,
        "7": 1,
        "10": 1
    },
    "errorMessage": null,
    "mostFrequentlyOccuringWordLength": [
        {
            "2": 4
        }
    ]
}
```

----------

## How it works:

The Rest controller endpoints delegate the request processing and production of
relevant response to the `BookService`.

This is responsible for parsing the body of the request into a  
{@link TextBook} instance, 
which will contain either the raw text passed in, or the text contents of the 
content that was downloaded from the URL or the Multipart file uploaded.

It will then process the TextBook where it will calculate:
- Total WordCount
- Average word Length
- Most frequent word length by count(s) (Could have a two or more ways tie in this).
- Tally of number of words per word count.
  -- E.g.
  {@code
  {
  {1, 5},
  {2, 4},
  {3, 6},
  ...
  }
  }

The above indicates a map keyed by the word length, and valued by the number of words at each length.
So the above shows an analysed text book that contained:
- 5 words of length 1
- 4 words of length 2
- 6 words of length 3
- etc...

## Definition of a "WORD"
For the purposes of this counter, a "word" is defined as:

- Any single, two or more consecutive characters 
- EITHER _followed by_ 
- OR _preceded AND followed_ by _one or more consecutive_ **Delimiter** characters
- AND NOT _containing_ ANY of the **Delimiter** characters.

I.e.
" I " -> is a single character word, in between two white spaces. Since white space is a
DELIMITER, then the character "I" is a single word, preceded AND followed by two delimiting characters.

### LIST OF DELIMITERS (including white space):

`(){}[]¬!*+-_=|~\^<>.?;:"~ `

## How to launch

I have added a `Dockerfile` along with a `docker-compose.yml` configuration, to build a fully self-contained 
image and launch locally on a computer with Docker installed. 

If that is not feasible, fear not!. 

The maven wrapper package stage will build a **fat-jar** with all dependencies included and 
with an embedded Tomcat server.

Running with docker-compose will give you a bit more flexibility, as you will be able to configure the local host 
port you want to bind to the image host server.

Tomcat by default will run on port 8080, on the image, OR when run locally via the fat-jar.

Docker compose on the other hand, will look for an environment variable to read the local port binding from, namely:
- WORDCOUNTER_PORT

If this is not found in the shell environment, it will look in the `.env` file. Finally it will fall back to port 8080 on the localhost
by default, if none of the above are not found.

To run any of the following configurations you need a Java 11 environment.

### Check out the project with

Download the project from github via: 

```
git-clone https://github.com/ta6hbe/wordcounter.git
```

### build - package and run locally - FAT JAR.

- From within the project root folder run:
```
/mvnw clean package && java -jar target/wordcounter-0.0.1-SNAPSHOT.jar
```

Your service should be up and running directly on your localhost on port 8080.

On your browser navigate to https://localhost:8080 and you should see an error page with the content as follows.
No worries. Our service is up and running. It is just not listening for GET HTTP requests.

```
Whitelabel Error Page
This application has no explicit mapping for /error, so you are seeing this as a fallback.

Tue Sep 06 21:58:03 BST 2022
There was an unexpected error (type=Not Found, status=404).

```

To get to this stage, the Maven Wrapper has run all the unit tests and they should have already passed.

You can start testing the service by sending POST Http requests, as per the docs above. You can either use the curl command snippets
included above as a guide, or use Postman. For ease of use I have exported a Postman Collection Json file and included with this 
project under the folder `postman`.

Remember for the service to launch successfully you need port 8080 free and available on the local host.

Happy testing.

### Using Docker compose

With `docker-compose`, the service will bind to port 9090 on the localhost, by default, by virtue of the `.env` file included.
If that port is available on the local host, simply run:

```
docker-compose up --build
```

This will build the container image, run all the unit tests before launching the service inside the container.
You need Docker installed and running on the host computer for this. You also need port 9090 available.

Alternatively you can define your own local port binding on the fly by using:

```
WORDCOUNTER_PORT=<You Port Choice here> docker-compose up --build
```

This way the local port will bind to whatever value you passed in the WORDCOUNTER_PORT command line argument, if the port is available.

To run HTTP POST requests against the service, you can still use the Postman collection included, making sure 
to change the **URL port** to whatever port you have launched to.

Also make sure you provide your own file examples. I have included some text files for proof of concept, available in 
folder `src/test/resources`.

You can supply these directly selecting then in the Postman UI, or if you use the CURL commands above, make sure to provide
correct file path information.

# Conclusion:

This is a complete Rest Service application covering the requirements of the word count problem stated.
The application is complete with:
- Backed by a Maven Wrapper build based on a POM file.
- Non-blocking IO for file transfers and async controller responses with Completable Futures.
- Unit tests and Mock MVC tests
- Documentation on public classes and public interfaces.
- Postman HTTP Call collections included as Integration tests.
- Curl call test examples.
- Fat Jar configuration with Embedded Tomcat server for launch portability.
- Docker and Docker-compose configurations for further launch portability, flexibility and reliability.
- API documentation provided via this README file. 

I hope you enjoy using this service, and I look forward to your feedback and possible correction / improvements that could be done.

