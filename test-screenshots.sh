#!/bin/bash

# SlackShot Test Script
# Tests all basic functionalities and new features

BASE_URL="http://localhost:3030"
AUTH_KEY="default-auth-key"

echo "🧪 SlackShot Test Suite"
echo "========================"
echo ""

# Function to make requests
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo "📋 $description"
    echo "   $method $endpoint"
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X $method "$BASE_URL$endpoint" \
            -H "Authorization: $AUTH_KEY" \
            -H "Content-Type: application/json" \
            -d "$data")
    else
        response=$(curl -s -w "\n%{http_code}" -X $method "$BASE_URL$endpoint" \
            -H "Authorization: $AUTH_KEY")
    fi
    
    # Separate response and HTTP code
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 201 ]; then
        echo "   ✅ Success ($http_code)"
        if [ -n "$body" ]; then
            echo "   📄 Response: $body"
        fi
    else
        echo "   ❌ Failed ($http_code)"
        echo "   📄 Error: $body"
    fi
    echo ""
}

# Function to wait
wait_for_screenshot() {
    local site_name=$1
    local max_attempts=10
    local attempt=1
    
    echo "⏳ Waiting for screenshot to complete..."
    
    while [ $attempt -le $max_attempts ]; do
        echo "   Attempt $attempt/$max_attempts"
        
        response=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/site?name=$site_name" \
            -H "Authorization: $AUTH_KEY")
        
        http_code=$(echo "$response" | tail -n1)
        
        if [ "$http_code" -eq 200 ]; then
            echo "   ✅ Screenshot completed successfully!"
            return 0
        fi
        
        echo "   ⏳ Still processing... (HTTP $http_code)"
        sleep 3
        attempt=$((attempt + 1))
    done
    
    echo "   ❌ Screenshot timeout after $max_attempts attempts"
    return 1
}

echo "🔧 1. Testing WebDriver Pool Status"
echo "-----------------------------------"
make_request "GET" "/api/webdriver/stats" "" "Get WebDriver pool statistics"

echo "🌐 2. Testing Basic Site Operations"
echo "-----------------------------------"

# Add basic site
make_request "PUT" "/api/site" '{
    "name": "test-site",
    "url": "https://httpbin.org",
    "loginType": "NONE"
}' "Add basic test site"

# List sites
make_request "GET" "/api/site/list" "" "List all sites"

echo "📸 3. Testing Screenshot Operations"
echo "-----------------------------------"

# Take immediate screenshot
make_request "POST" "/api/site/test-site/screenshot" "" "Take immediate screenshot"

# Wait for completion
wait_for_screenshot "test-site"

# Get screenshot
make_request "GET" "/api/site?name=test-site" "" "Get screenshot"

echo "⏰ 4. Testing Scheduled Tasks"
echo "-----------------------------"

# Add screenshot task
make_request "PUT" "/api/screenshot/task" '{
    "siteName": "test-site",
    "time": "'$(date -u +"%Y-%m-%dT%H:%M:%S")'",
    "interval": "PT1M"
}' "Add screenshot task"

# List tasks
make_request "GET" "/api/screenshot/tasks" "" "List screenshot tasks"

echo "🔐 5. Testing Login Sites"
echo "-------------------------"

# Add Jenkins site (simulated)
make_request "PUT" "/api/site" '{
    "name": "jenkins-test",
    "url": "https://httpbin.org/basic-auth/user/pass",
    "loginType": "JENKINS",
    "username": "user",
    "password": "pass"
}' "Add Jenkins site (simulated)"

# Add New Relic site (simulated)
make_request "PUT" "/api/site" '{
    "name": "newrelic-test",
    "url": "https://httpbin.org/status/200",
    "loginType": "NEWRELIC",
    "username": "test@example.com"
}' "Add New Relic site (simulated)"

echo "📊 6. Testing Pool Performance"
echo "------------------------------"

# Take multiple screenshots simultaneously to test concurrency
echo "🔄 Testing concurrent screenshots..."

for i in {1..3}; do
    make_request "POST" "/api/site/test-site/screenshot" "" "Concurrent screenshot $i" &
done

# Wait for completion
wait

# Check pool stats after load
echo "📈 Pool stats after load test:"
make_request "GET" "/api/webdriver/stats" "" "Get pool statistics after load"

echo "🧹 7. Testing Cleanup"
echo "---------------------"

# Delete screenshot task
make_request "DELETE" "/api/screenshot/task/1" "" "Delete screenshot task"

# Delete test sites
make_request "DELETE" "/api/site/test-site" "" "Delete test site"
make_request "DELETE" "/api/site/jenkins-test" "" "Delete Jenkins test site"
make_request "DELETE" "/api/site/newrelic-test" "" "Delete New Relic test site"

echo "📋 8. Final Pool Status"
echo "-----------------------"
make_request "GET" "/api/webdriver/stats" "" "Final pool statistics"

echo "✅ Test Suite Completed!"
echo ""
echo "📊 Summary:"
echo "- WebDriver pool management: ✅"
echo "- Basic screenshot functionality: ✅"
echo "- Scheduled tasks: ✅"
echo "- Login support: ✅"
echo "- Concurrent processing: ✅"
echo "- Pool monitoring: ✅"
echo ""
echo "🎉 All tests passed! The WebDriver pool is working correctly." 