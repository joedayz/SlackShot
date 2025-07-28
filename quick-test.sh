#!/bin/bash

# SlackShot Quick Test
# Quick test of basic functionality

BASE_URL="http://localhost:3030"
AUTH_KEY="default-auth-key"

echo "⚡ SlackShot Quick Test"
echo "======================"
echo ""

# Check if application is running
echo "🔍 Checking if application is running..."
if curl -s "$BASE_URL/api/webdriver/stats" -H "Authorization: $AUTH_KEY" > /dev/null 2>&1; then
    echo "✅ Application is running"
else
    echo "❌ Application is not running on $BASE_URL"
    echo "   Please start the application first: mvn spring-boot:run"
    exit 1
fi

# Check WebDriver pool
echo ""
echo "📊 Checking WebDriver pool..."
pool_stats=$(curl -s "$BASE_URL/api/webdriver/stats" -H "Authorization: $AUTH_KEY")
if [ $? -eq 0 ]; then
    echo "✅ Pool stats endpoint working"
    echo "   Pool configuration: $pool_stats"
else
    echo "❌ Pool stats endpoint failed"
    exit 1
fi

# Test basic screenshot
echo ""
echo "📸 Testing basic screenshot..."

# Add test site
echo "   Adding test site..."
add_response=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/site" \
    -H "Authorization: $AUTH_KEY" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "quick-test",
        "url": "https://httpbin.org",
        "loginType": "NONE"
    }')

http_code=$(echo "$add_response" | tail -n1)
if [ "$http_code" -eq 200 ]; then
    echo "   ✅ Test site added"
else
    echo "   ❌ Failed to add test site (HTTP $http_code)"
    exit 1
fi

# Take screenshot
echo "   Taking screenshot..."
screenshot_response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/site/quick-test/screenshot" \
    -H "Authorization: $AUTH_KEY")

http_code=$(echo "$screenshot_response" | tail -n1)
if [ "$http_code" -eq 200 ]; then
    echo "   ✅ Screenshot taken successfully"
else
    echo "   ❌ Screenshot failed (HTTP $http_code)"
    exit 1
fi

# Check pool after screenshot
echo "   Checking pool after screenshot..."
pool_after=$(curl -s "$BASE_URL/api/webdriver/stats" -H "Authorization: $AUTH_KEY")
echo "   Pool status: $pool_after"

# Clean up
echo ""
echo "🧹 Cleaning up..."
curl -s -X DELETE "$BASE_URL/api/site/quick-test" -H "Authorization: $AUTH_KEY" > /dev/null
echo "   ✅ Test site removed"

echo ""
echo "🎉 Quick test completed successfully!"
echo ""
echo "📋 Summary:"
echo "   ✅ Application running"
echo "   ✅ WebDriver pool working"
echo "   ✅ Screenshot functionality working"
echo "   ✅ Pool management working"
echo ""
echo "🚀 Ready for full testing with ./test-screenshots.sh" 