# Convert Wikipedia Dumps to Neo4j Loadable CSVs

This application enables you to transform and download Wikipedia dumps into Neo4j loadable CSVs. The main method's
inputFile variable can be configured to suit your needs. If you only need the results once, using the remote version of
the InputFile is recommended, as it automatically downloads the latest dump. For multiple runs, such as for testing or
adding features to the code, it's advisable to download and unzip the files beforehand to optimize performance.

The CSV files will be output to the data/{wiki} directory.

## Options

Local file URIs expect the files to be located in the {user_home}/wikidata folder.

| InputFile              | URI                                                                                    | Output Folder | FileLoader | Expected Runtime |
|------------------------|----------------------------------------------------------------------------------------|---------------|------------|------------------|
| EN_WIKI                | enwiki-latest-pages-articles.xml                                                       | en_wiki       | PLAIN      |                  |
| EN_WIKI_BZ2            | enwiki-latest-pages-articles.xml.bz2                                                   | en_wiki       | BZ2        |                  |
| EN_WIKI_BZ2_REMOTE     | https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2         | en_wiki       | REMOTE_BZ2 | 1h 30min         |
| SIMPLE_WIKI            | simplewiki-latest-pages-articles.xml                                                   | simple_wiki   | PLAIN      | 20s              |
| SIMPLE_WIKI_BZ2        | simplewiki-latest-pages-articles.xml.bz2                                               | simple_wiki   | BZ2        | 50s              |
| SIMPLE_WIKI_BZ2_REMOTE | https://dumps.wikimedia.org/simplewiki/latest/simplewiki-latest-pages-articles.xml.bz2 | simple_wiki   | REMOTE_BZ2 | 4min             |

## Output Data

The output consists of two types of CSV files: "links" and "pages", both chunked at 256 megabytes. The format is
consistent with their respective header files located under `headers/`.

### Pages

The format of the pages output CSVs is defined in `headers/pages_headers.csv`.

| Field              | Example     | Description                                                                                              |
|--------------------|-------------|----------------------------------------------------------------------------------------------------------|
| urlTitle:ID        | Alan_Turing | This field is used as a unique ID in Neo4j and is the title formatted to work in the Wikipedia URL path. |
| title:string       | Alan Turing | The title of the page.                                                                                   |
| pageId:long        | 1261710     | Wikipedia's internal page ID, which can be used with the Wikipedia API.                                  |
| isRedirect:boolean | false       | Indicates if the page is a redirect to another page.                                                     |

### Links

The format of the links output CSVs is defined in `headers/links_headers.csv`.

| Field              | Example      | Description                                          |
|--------------------|--------------|------------------------------------------------------|
| :START_ID          | Alan_Turing  | The URL title of the page where the link is located. |
| :END_ID            | Human_rights | The URL title of the target page.                    |
| text:string        | human rights | The link text visible on the website.                |
| isRedirect:boolean | false        | Indicates if the :START_ID page is a redirect page.  |
| index:long         | 45           | The position of the link on the page.                |

## Dumps

You can find dump files at https://dumps.wikimedia.org/.

| wiki       | url                                                                                    |
|------------|----------------------------------------------------------------------------------------|
| enwiki     | https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2         |
| simplewiki | https://dumps.wikimedia.org/simplewiki/latest/simplewiki-latest-pages-articles.xml.bz2 |

## Expected Disk/Network Usage

| InputFile        | Input File Size | Total CSV Size |
|------------------|-----------------|----------------|
| EN_WIKI          | 87G             | 20G            |
| EN_WIKI_BZ2*     | 19G             | 20G            |
| SIMPLE_WIKI      | 1.1G            | 333M           |
| SIMPLE_WIKI_BZ2* | 236M            | 333M           |

## Neo4j Import Usage

| wiki        | Import Time | Node Count | Relationship Count | Data Volume Size |
|-------------|-------------|------------|--------------------|------------------|
| en_wiki     | 13min 57s   | 22781670   | 235766142          | 25.8G            |
| simple_wiki | 40s         | 430468     | 3353337            | 900M             |
