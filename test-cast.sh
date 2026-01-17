#!/bin/bash

# Test script for FrequentSee TV Agent
# Usage: ./test-cast.sh <TV_IP_ADDRESS>

if [ -z "$1" ]; then
    echo "Usage: $0 <TV_IP_ADDRESS>"
    echo "Example: $0 192.168.1.100"
    exit 1
fi

TV_IP=$1
CAST_URL="http://$TV_IP:8080/cast"
STOP_URL="http://$TV_IP:8080/stop"
STATUS_URL="http://$TV_IP:8080/status"

echo "FrequentSee TV Agent - Test Script"
echo "==================================="
echo ""

# Test status endpoint
echo "1. Checking status..."
curl -s "$STATUS_URL" | python3 -m json.tool 2>/dev/null || curl -s "$STATUS_URL"
echo ""
echo ""

# Test cast endpoint with Apple's sample HLS stream
echo "2. Starting test stream..."
curl -X POST "$CAST_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "streamUrl": "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8",
    "title": "Test Stream",
    "subtitle": "Apple Sample HLS Stream"
  }' | python3 -m json.tool 2>/dev/null || curl -X POST "$CAST_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "streamUrl": "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8",
    "title": "Test Stream",
    "subtitle": "Apple Sample HLS Stream"
  }'
echo ""
echo ""

echo "3. Waiting 10 seconds..."
sleep 10

# Check status again
echo "4. Checking status..."
curl -s "$STATUS_URL" | python3 -m json.tool 2>/dev/null || curl -s "$STATUS_URL"
echo ""
echo ""

echo "5. Stopping playback..."
curl -X POST "$STOP_URL" | python3 -m json.tool 2>/dev/null || curl -X POST "$STOP_URL"
echo ""
echo ""

echo "Test completed!"
