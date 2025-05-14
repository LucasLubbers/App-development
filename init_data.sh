#!/bin/bash

# Wait for PocketBase to start
sleep 5

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

# Check if the collection exists
COLLECTION_EXISTS=$(curl -s -X GET http://localhost:8090/api/collections/workouts \
  -H "Authorization: Bearer $TOKEN")

if echo "$COLLECTION_EXISTS" | grep -q '"code":'; then
  # If the collection doesn't exist, create it
  COLLECTION_CREATE_RESPONSE=$(curl -s -X POST http://localhost:8090/api/collections \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
      "name": "workouts",
      "type": "base"
    }')

  echo "Collection creation response: $COLLECTION_CREATE_RESPONSE"  # Debug output

  if echo "$COLLECTION_CREATE_RESPONSE" | grep -q '"code":'; then
    echo "Failed to create collection: $COLLECTION_CREATE_RESPONSE"
    exit 1
  fi

  echo "Collection created successfully."
  COLLECTION_ID=$(echo "$COLLECTION_CREATE_RESPONSE" | jq -r '.id')

else
  # If the collection exists, get its ID
  COLLECTION_ID=$(echo "$COLLECTION_EXISTS" | jq -r '.id')
  echo "Collection already exists. Using ID: $COLLECTION_ID"
fi

# Define the fields to be added
UPDATE_FIELDS='{
  "fields": [
    {
      "name": "name",
      "type": "text",
      "required": true,
      "unique": false
    },
    {
      "name": "difficulty",
      "type": "number",
      "required": true,
      "unique": false
    }
  ]
}'

# Update the collection to add the fields (name and difficulty)
UPDATE_COLLECTION_RESPONSE=$(curl -s -X PATCH http://localhost:8090/api/collections/$COLLECTION_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "$UPDATE_FIELDS")

echo "Collection update response: $UPDATE_COLLECTION_RESPONSE"  # Debug output

# Check if the collection update was successful
if echo "$UPDATE_COLLECTION_RESPONSE" | grep -q '"code":'; then
  echo "Failed to update collection: $UPDATE_COLLECTION_RESPONSE"
  exit 1
fi

echo "Collection updated successfully for collection: $COLLECTION_ID"

# Fetch the collection schema to verify fields
COLLECTION_SCHEMA=$(curl -s -X GET "http://localhost:8090/api/collections/$COLLECTION_ID" \
  -H "Authorization: Bearer $TOKEN")

echo "Collection schema: $COLLECTION_SCHEMA"  # Debug output

# Delete existing records (if any)
EXISTING_IDS=$(curl -s -X GET "http://localhost:8090/api/collections/workouts/records?perPage=200" \
  -H "Authorization: Bearer $TOKEN" | jq -r '.items[].id')

for id in $EXISTING_IDS; do
  curl -s -X DELETE "http://localhost:8090/api/collections/workouts/records/$id" \
    -H "Authorization: Bearer $TOKEN"
done

# Add multiple records to the collection
RECORDS=(
  '{"name": "Push-ups", "difficulty": 3}'
  '{"name": "Squats", "difficulty": 2}'
  '{"name": "Lunges", "difficulty": 4}'
)

for RECORD in "${RECORDS[@]}"; do
  ADD_RECORD_RESPONSE=$(curl -s -X POST http://localhost:8090/api/collections/workouts/records \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "$RECORD")

  if echo "$ADD_RECORD_RESPONSE" | grep -q '"code":'; then
    echo "Failed to add record: $ADD_RECORD_RESPONSE"
    exit 1
  fi
done

echo "Initialization completed successfully."
