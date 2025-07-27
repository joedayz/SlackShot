#!/bin/bash

# SlackShot API Example Usage
# This script demonstrates how to use the SlackShot API

BASE_URL="http://localhost:3030"
AUTH_KEY="your-auth-key-here"

echo "SlackShot API Example Usage"
echo "=========================="

# Function to make authenticated requests
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    if [ -n "$data" ]; then
        curl -X $method "$BASE_URL$endpoint" \
            -H "Authorization: $AUTH_KEY" \
            -H "Content-Type: application/json" \
            -d "$data"
    else
        curl -X $method "$BASE_URL$endpoint" \
            -H "Authorization: $AUTH_KEY"
    fi
    echo -e "\n"
}

echo "1. Adding a new site..."
make_request "PUT" "/api/site" '{
    "name": "example-site",
    "url": "https://httpbin.org",
    "loginType": "NONE"
}'

echo "2. Adding a site with Jenkins login..."
make_request "PUT" "/api/site" '{
    "name": "jenkins-site",
    "url": "https://jenkins.example.com",
    "loginType": "JENKINS",
    "username": "admin",
    "password": "password123"
}'

echo "3. Adding New Relic dashboard..."
make_request "PUT" "/api/site" '{
    "name": "newrelic-cost-manager",
    "url": "https://one.newrelic.com/dashboards/detail/MTQxMjg3M3xWSVp8REFTSEJPQVJEfGRhOjQ0MzcwNDk?duration=86400000&state=d0fbd92c-84ed-0ee6-7512-3526b1527949",
    "loginType": "NEWRELIC",
    "username": "ext_jamadeo@dlocal.com"
}'

echo "4. Getting all sites..."
make_request "GET" "/api/site/list"

echo "5. Taking a screenshot immediately..."
make_request "POST" "/api/site/example-site/screenshot"

echo "6. Adding a screenshot task..."
make_request "PUT" "/api/screenshot/task" '{
    "siteName": "example-site",
    "time": "2024-01-01T10:00:00",
    "interval": "PT1H"
}'

echo "7. Adding a Slack task for New Relic..."
make_request "PUT" "/api/slack/task" '{
    "siteName": "newrelic-cost-manager",
    "time": "2024-01-01T10:00:00",
    "interval": "PT1H",
    "slackToken": "xoxb-your-slack-token",
    "slackChannel": "#monitoring"
}'

echo "8. Getting all screenshot tasks..."
make_request "GET" "/api/screenshot/tasks"

echo "9. Getting all Slack tasks..."
make_request "GET" "/api/slack/tasks"

echo "10. Getting the latest screenshot..."
make_request "GET" "/api/site?name=example-site"

echo "Example usage completed!"
echo "Check the application logs for more details." 