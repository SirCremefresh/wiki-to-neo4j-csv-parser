# Convert Wikipedia Dumps to Neo4j Loadable CSVs

This application enables you to transform and download Wikipedia dumps into Neo4j loadable CSVs. If you only need the
results once, using the remote version of the InputFile is recommended, as it automatically downloads the latest dump.
For multiple runs, such as for testing or adding features to the code, it's advisable to download and unzip the files
beforehand to optimize performance.

The CSV files will be output to the data/{wiki} directory.

## Options

Local file URIs expect the files to be located in the {user_home}/wikidata folder.

| InputFile                        | URI                                                                                    | Output Folder | FileLoader | Expected Runtime |
|----------------------------------|----------------------------------------------------------------------------------------|---------------|------------|------------------|
| EN_WIKI                          | enwiki-latest-pages-articles.xml                                                       | en_wiki       | PLAIN      | 20min            |
| EN_WIKI_BZ2                      | enwiki-latest-pages-articles.xml.bz2                                                   | en_wiki       | BZ2        | 1h               |
| EN_WIKI_BZ2_REMOTE               | https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2         | en_wiki       | REMOTE_BZ2 | 1h 30min         |
| SIMPLE_WIKI                      | simplewiki-latest-pages-articles.xml                                                   | simple_wiki   | PLAIN      | 20s              |
| SIMPLE_WIKI_BZ2                  | simplewiki-latest-pages-articles.xml.bz2                                               | simple_wiki   | BZ2        | 50s              |
| SIMPLE_WIKI_BZ2_REMOTE (Default) | https://dumps.wikimedia.org/simplewiki/latest/simplewiki-latest-pages-articles.xml.bz2 | simple_wiki   | REMOTE_BZ2 | 4min             |

To run the application, use the following command:
```shell
mvn compile exec:java -Dexec.args="{InputFile}"
```

Sample:
```shell
mvn compile exec:java -Dexec.args="SIMPLE_WIKI_BZ2_REMOTE"
```

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
| en_wiki     | 14min       | 22781670   | 235766142          | 26G              |
| simple_wiki | 40s         | 430468     | 3353337            | 900M             |

## Sample Importing CSVs in docker container

There is a minimal docker compose configuration for neo4j in the file: [docker-compose.yaml](docker-compose.yaml).
You can run the following commands to set up neo4j and import the CSVs.

If you want to use password authentication you have to create a file called ".env" in the same directory as the
docker-compose.yaml file. The file should look like this:
```
NEO4J_AUTH=neo4j/YOUR_PASSWORD
```
It is important that the username is "neo4j" otherwise neo4j will not start.

### Setup & Import Simple-Wiki

```shell
echo "Remove existing installation if exists"
docker compose down --volumes
docker compose rm --volumes --stop --force

echo "Start and Stop Neo4j to initialize the volumes"
docker compose up --detach --wait
docker compose down

echo "Import the CSV files"
header_dir="$(pwd)/headers"
data_dir="$(pwd)/data/simple_wiki"
report="$(pwd)/import.report"
echo -n "" >"${report}"
docker run --interactive --tty --rm \
  --volume=wiki-to-neo4j-csv-parser_data:/data \
  --volume="${data_dir}":/import \
  --volume="${header_dir}":/import-headers \
  --volume="${report}":/var/lib/neo4j/import.report \
  neo4j:5.18 \
  neo4j-admin database import full --overwrite-destination --bad-tolerance=10000000000 \
  --nodes=Page=/import-headers/page_headers.csv,/import/pages-\\d+.csv \
  --relationships=LINKS_TO=/import-headers/link_headers.csv,/import/links-\\d+.csv \
  --skip-bad-relationships

```

### Setup & Import En-Wiki

```shell
echo "Remove existing installation if exists"
docker compose down --volumes
docker compose rm --volumes --stop --force

echo "Start and Stop Neo4j to initialize the volumes"
docker compose up --detach --wait
docker compose down

echo "Import the CSV files"
header_dir="$(pwd)/headers"
data_dir="$(pwd)/data/en_wiki"
report="$(pwd)/import.report"
echo -n "" >"${report}"
docker run --interactive --tty --rm \
  --volume=wiki-to-neo4j-csv-parser_data:/data \
  --volume="${data_dir}":/import \
  --volume="${header_dir}":/import-headers \
  --volume="${report}":/var/lib/neo4j/import.report \
  neo4j:5.18 \
  neo4j-admin database import full --overwrite-destination --bad-tolerance=10000000000 \
  --nodes=Page=/import-headers/page_headers.csv,/import/pages-\\d+.csv \
  --relationships=LINKS_TO=/import-headers/link_headers.csv,/import/links-\\d+.csv \
  --skip-bad-relationships

```

### Start

The admin GUI is available on [http://localhost:7474/browser/](http://localhost:7474/browser/) you can authenticate with
the "authentication type" "no authentication" or "username / password" depending on if you have set a password in the
".env" file.
The database can be reached on port 7687.

```shell
docker compose up --detach --wait
```

### Stop

```shell
docker compose down
```

## Sample Cypher

### Create Index

When you want to do path searches over the "urlTitle" it is advised to create an index on it to increase the lookup
speed.

```
CREATE CONSTRAINT pages_urlTitle
FOR (p:Page) REQUIRE p.urlTitle IS UNIQUE
```

### Find Page

```
MATCH (p:Page { urlTitle: 'Switzerland' })
RETURN p
```

### Find Shortest Path

```
MATCH path=shortestPath((start:Page)-[:LINKS_TO*1..20]->(end:Page))
WHERE start.urlTitle = "Switzerland" AND end.urlTitle = "United_States"
RETURN path
```
