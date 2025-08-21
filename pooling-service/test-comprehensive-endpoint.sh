#!/bin/bash

# Test script for the new comprehensive trip endpoint

echo "Testing the comprehensive trip endpoint..."

# Test with sample data
TRIP_ID="trip_001"
USER_ID="user_123"
BASE_URL="http://localhost:8080"

# Test the new comprehensive endpoint
echo "Testing GET /api/v1/public-pooling/trips/${TRIP_ID}/comprehensive?userId=${USER_ID}"

curl -X GET "${BASE_URL}/api/v1/public-pooling/trips/${TRIP_ID}/comprehensive?userId=${USER_ID}" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n" \
  -s | jq '.' || echo "Response (raw):"

echo ""
echo "Test completed."
