#!/usr/bin/env sh
set -e

ACCEPTED_WIKIS="en_wiki simple_wiki"

show_help() {
    echo "Description:"
    echo "  This program initializes the Docker Compose with Neo4j using"
    echo "  the previously exported wiki data from the ./data folder."
    echo ""
    echo "Usage: script.sh --wiki <WIKI_VALUE> --password <PASSWORD_VALUE>"
    echo "Options:"
    echo "  --wiki     Wiki value. Accepted values: $ACCEPTED_WIKIS."
    echo "  --password Password value (at least 8 characters). Use 'none' for no authentication."
    echo "  --help, -h Display this help and exit."
}

# Check if value is in the array
contains() {
    value="$1"
    for item in $ACCEPTED_WIKIS; do
        [ "$item" = "$value" ] && return 0
    done
    return 1
}

# Parse parameters
while [ "$#" -gt 0 ]; do
    case $1 in
        --wiki) WIKI="$2"; shift ;;
        --password) PASSWORD="$2"; shift ;;
        --help|-h) show_help; exit 0 ;;
        *) echo "Error: Unknown parameter '$1'."; show_help; exit 1 ;;
    esac
    shift
done

# Validate parameters
if [ -z "$WIKI" ] || [ -z "$PASSWORD" ]; then
    echo "Error: Missing parameters."
    show_help
    exit 1
fi

# Check if WIKI value is accepted
if ! contains "$WIKI"; then
    echo "Error: Invalid wiki value."
    show_help
    exit 1
fi

# Validate password length
if [ "$PASSWORD" != "none" ] && [ ${#PASSWORD} -lt 8 ]; then
    echo "Error: Password must be at least 8 characters long."
    show_help
    exit 1
fi

if [ "$PASSWORD" = "none" ]; then
    echo "Initializing Neo4j with wiki: $WIKI and no authentication."
    [ -f .env ] && rm .env
else
    echo "Initializing Neo4j with wiki: $WIKI and password authentication."
    echo "NEO4J_AUTH=neo4j/$PASSWORD" > .env
fi

echo "Removing any existing installations..."
docker compose down --volumes
docker compose rm --volumes --stop --force

echo "Starting Neo4j to initialize the volumes..."
docker compose up --detach --wait

echo "Stopping Neo4j..."
docker compose down

echo "Importing the CSV files..."
header_dir="$(pwd)/headers"
data_dir="$(pwd)/data/$WIKI"
report="$(pwd)/import.report"
echo "" >"${report}"
docker run --interactive --tty --rm \
  --volume=wiki-to-neo4j-csv-parser_data:/data \
  --volume="${data_dir}":/import \
  --volume="${header_dir}":/import-headers \
  --volume="${report}":/var/lib/neo4j/import.report \
  neo4j:5.11 \
  neo4j-admin database import full --overwrite-destination --bad-tolerance=10000000000 \
  --nodes=Page=/import-headers/page_headers.csv,/import/pages-\\d+.csv \
  --relationships=LINKS_TO=/import-headers/link_headers.csv,/import/links-\\d+.csv \
  --skip-bad-relationships

echo "Starting Neo4j..."
docker compose up --detach --wait

echo "Creating indexes..."
CYPHER_QUERY="CREATE CONSTRAINT pages_urlTitle FOR (p:Page) REQUIRE p.urlTitle IS UNIQUE;"

if [ "$PASSWORD" = "none" ]; then
    echo "$CYPHER_QUERY" | docker exec --interactive wiki-to-neo4j-csv-parser-neo4j-1 cypher-shell
else
    echo "$CYPHER_QUERY" | docker exec --interactive wiki-to-neo4j-csv-parser-neo4j-1 cypher-shell -u neo4j -p "$PASSWORD"
fi

echo "Done!"
