# Word Count Service APP:
### Author: C Perperidis
### email: ta6hbe@hotmail.com
### date: Tuesday 06/09/2022

## Intro:
This is a simple SpringBoot App, implementing a **Word Counting** service exposed via a 
REST API publishing two HTTP POST endpoints.
- /count/text
- /count/file

This can be downloaded from my Github repo here.

I have made an effort to make the endpoints and services as efficient as possible, by 
using NON Blocking IO for File and HTTP download operations.

The service and controller endpoints also return CompletableFuture(s) so that any processing
can be completed and dispatched asynchronously, without blocking the controller.

It uses maven and contains a `pom.xml` file. It builds a self-contained executable
jar which runs an embedded Tomcat server exposing port 8080 on the local host.

If you have any other process using port 8080, you will need to stop it, before
running this. In the future I intend to make this a configurable part, so you should
be able to choose which port you want to configure the embedded server to listen to.

Future improvements may include (but not limited to):
- Adding environment based configuration for configuring embedded web server port.
- Expanding the endpoint request to contain locale information based on which to choose charset and delimiters.
- Adding further endpoints for other book functions... 

To see details for how to build, test, run and use see the HOW_TO_RUN_ME.md file.

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
{@link com.synalogic.hperperidis.wordcounter.model.TextBook} instance, 
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

### Definition of a "WORD"
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

Enjoy using this app.

