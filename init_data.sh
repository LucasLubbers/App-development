#!/bin/bash

# Wait for PocketBase to start
echo "Waiting for PocketBase to start..."
until curl -s -o /dev/null -w "%{http_code}" http://localhost:8090/api/collections > /dev/null 2>&1; do
  echo "Waiting for PocketBase to be ready..."
  sleep 1
done
echo "PocketBase is up and running!"

# Load environment variables from .env file
if [ -f /pb/.env ]; then
  export $(grep -v '^#' /pb/.env | xargs)
else
  echo ".env file not found!"
  exit 1
fi

# Use the API key from the .env file
TOKEN=$API_KEY

if [ -z "$TOKEN" ]; then
  echo "API key not found in .env file!"
  exit 1
fi

# === Define all collections, fields, and records ===
declare -A COLLECTIONS

COLLECTIONS["workouts"]='{
  "fields": [
    { "name": "name", "type": "text", "required": false, "unique": false },
    { "name": "difficulty", "type": "number", "required": false, "unique": false }
  ],
  "records": [
    { "name": "Push-ups", "difficulty": 3 },
    { "name": "Squats", "difficulty": 2 },
    { "name": "Lunges", "difficulty": 4 },
    { "name": "Sit-ups", "difficulty": 2 }
  ]
}'

COLLECTIONS["goals"]='{
  "fields": [
    { "name": "description", "type": "text", "required": false, "unique": false },
    { "name": "completed", "type": "bool", "required": false, "unique": false }
  ],
  "records": [
    { "description": "Run 5k", "completed": false },
    { "description": "30 day yoga challenge", "completed": true }
  ]
}'

# === Loop through each collection and apply logic ===
for COLLECTION_NAME in "${!COLLECTIONS[@]}"; do
  CONFIG_JSON="${COLLECTIONS[$COLLECTION_NAME]}"

  echo "Processing collection: $COLLECTION_NAME"

  # Check if collection exists
  HTTP_STATUS=$(curl -s -o response.json -w "%{http_code}" -X GET "http://localhost:8090/api/collections/$COLLECTION_NAME" \
    -H "Authorization: Bearer $TOKEN")

  if [ "$HTTP_STATUS" = "404" ]; then
    # Create collection if not exists
    CREATE_RESP=$(curl -s -X POST http://localhost:8090/api/collections \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d "{\"name\": \"$COLLECTION_NAME\", \"type\": \"base\"}")

    if echo "$CREATE_RESP" | jq -e '.code?' > /dev/null; then
      echo "Failed to create $COLLECTION_NAME: $CREATE_RESP"
      exit 1
    fi

    echo "Created collection '$COLLECTION_NAME'"
    COLLECTION_ID=$(echo "$CREATE_RESP" | jq -r '.id')
  else
    COLLECTION_ID=$(jq -r '.id' < response.json)
    echo "Collection '$COLLECTION_NAME' already exists (ID: $COLLECTION_ID)"
  fi

  # Update fields
  FIELDS=$(echo "$CONFIG_JSON" | jq '{fields: .fields}')
  PATCH_RESP=$(curl -s -X PATCH "http://localhost:8090/api/collections/$COLLECTION_ID" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "$FIELDS")

  if echo "$PATCH_RESP" | jq -e '.code?' > /dev/null; then
    echo "Failed to update fields in $COLLECTION_NAME: $PATCH_RESP"
    exit 1
  fi

  echo "Fields updated in '$COLLECTION_NAME'"

  # Delete existing records
  RECORD_IDS=$(curl -s -X GET "http://localhost:8090/api/collections/$COLLECTION_NAME/records?perPage=200" \
    -H "Authorization: Bearer $TOKEN" | jq -r '.items[].id')

  for id in $RECORD_IDS; do
    curl -s -X DELETE "http://localhost:8090/api/collections/$COLLECTION_NAME/records/$id" \
      -H "Authorization: Bearer $TOKEN"
  done

  echo "Cleared existing records in '$COLLECTION_NAME'"

  # Add new records with correct formatting
  echo "$CONFIG_JSON" | jq -c '.records[]' | while IFS= read -r RECORD; do
    echo "Adding record: $RECORD"

    ADD_RESP=$(curl -s -X POST "http://localhost:8090/api/collections/$COLLECTION_NAME/records" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d "$RECORD")

    if echo "$ADD_RESP" | jq -e '.code?' > /dev/null; then
      echo "Failed to add record to $COLLECTION_NAME:"
      echo "Record: $RECORD"
      echo "Response: $ADD_RESP"
      exit 1
    else
      echo "Successfully added record to $COLLECTION_NAME: $RECORD"
    fi
  done


  echo "Added records to '$COLLECTION_NAME'"
done

echo "All collections initialized successfully."
