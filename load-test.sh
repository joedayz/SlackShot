#!/bin/bash

# SlackShot Load Test Script
# Tests WebDriver pool performance under load

BASE_URL="http://localhost:3030"
AUTH_KEY="default-auth-key"
CONCURRENT_REQUESTS=10
TOTAL_REQUESTS=50

echo "🚀 SlackShot Load Test"
echo "======================"
echo ""

# Function to make a screenshot request
take_screenshot() {
    local request_id=$1
    local start_time=$(date +%s%3N)
    
    response=$(curl -s -w "\n%{http_code}\n%{time_total}" -X POST "$BASE_URL/api/site/test-site/screenshot" \
        -H "Authorization: $AUTH_KEY")
    
    local end_time=$(date +%s%3N)
    local duration=$((end_time - start_time))
    
    # Separate response, HTTP code and curl time
    local http_code=$(echo "$response" | tail -n2 | head -n1)
    local curl_time=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | head -n -2)
    
    if [ "$http_code" -eq 200 ]; then
        echo "✅ Request $request_id: ${duration}ms (curl: ${curl_time}s)"
    else
        echo "❌ Request $request_id: Failed (HTTP $http_code) - ${duration}ms"
    fi
    
    echo "$request_id,$http_code,$duration,$curl_time" >> load_test_results.csv
}

# Function to get pool stats
get_pool_stats() {
    response=$(curl -s -X GET "$BASE_URL/api/webdriver/stats" \
        -H "Authorization: $AUTH_KEY")
    
    echo "$response"
}

# Create results file
echo "request_id,http_code,duration_ms,curl_time_s" > load_test_results.csv

echo "📊 Initial Pool Stats:"
initial_stats=$(get_pool_stats)
echo "$initial_stats"
echo ""

echo "🔄 Starting load test..."
echo "   Concurrent requests: $CONCURRENT_REQUESTS"
echo "   Total requests: $TOTAL_REQUESTS"
echo ""

# Create test site if it doesn't exist
echo "📋 Setting up test site..."
curl -s -X PUT "$BASE_URL/api/site" \
    -H "Authorization: $AUTH_KEY" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "test-site",
        "url": "https://httpbin.org",
        "loginType": "NONE"
    }' > /dev/null

echo "🚀 Executing load test..."

# Execute requests in concurrent batches
for ((i=1; i<=TOTAL_REQUESTS; i++)); do
    take_screenshot $i &
    
    # Control concurrency
    if (( i % CONCURRENT_REQUESTS == 0 )); then
        wait
        echo "   Completed batch $((i/CONCURRENT_REQUESTS))"
    fi
done

# Wait for all requests to complete
wait

echo ""
echo "📊 Final Pool Stats:"
final_stats=$(get_pool_stats)
echo "$final_stats"
echo ""

# Analyze results
echo "📈 Load Test Results:"
echo "===================="

if command -v awk >/dev/null 2>&1; then
    echo "Requests completed: $(tail -n +2 load_test_results.csv | wc -l)"
    echo "Successful requests: $(tail -n +2 load_test_results.csv | awk -F',' '$2==200' | wc -l)"
    echo "Failed requests: $(tail -n +2 load_test_results.csv | awk -F',' '$2!=200' | wc -l)"
    
    avg_duration=$(tail -n +2 load_test_results.csv | awk -F',' '{sum+=$3} END {print sum/NR}')
    echo "Average duration: ${avg_duration}ms"
    
    max_duration=$(tail -n +2 load_test_results.csv | awk -F',' 'BEGIN{max=0} {if($3>max) max=$3} END {print max}')
    echo "Maximum duration: ${max_duration}ms"
    
    min_duration=$(tail -n +2 load_test_results.csv | awk -F',' 'BEGIN{min=999999} {if($3<min) min=$3} END {print min}')
    echo "Minimum duration: ${min_duration}ms"
else
    echo "Results saved to load_test_results.csv"
    echo "Install awk for detailed analysis"
fi

echo ""
echo "🎯 Performance Analysis:"
echo "======================="

# Check if pool handled load correctly
success_rate=$(tail -n +2 load_test_results.csv | awk -F',' '$2==200' | wc -l)
total_requests=$(tail -n +2 load_test_results.csv | wc -l)
success_percentage=$((success_rate * 100 / total_requests))

echo "Success rate: ${success_percentage}%"

if [ $success_percentage -ge 95 ]; then
    echo "✅ Excellent performance! Pool handled load very well."
elif [ $success_percentage -ge 80 ]; then
    echo "✅ Good performance! Pool handled load well."
elif [ $success_percentage -ge 60 ]; then
    echo "⚠️  Acceptable performance. Consider tuning pool settings."
else
    echo "❌ Poor performance. Pool may need optimization."
fi

echo ""
echo "📋 Recommendations:"
echo "=================="

if [ $success_percentage -lt 80 ]; then
    echo "- Increase webdriver.pool.max-size in application.yml"
    echo "- Increase webdriver.pool.queue-capacity"
    echo "- Consider increasing webdriver.pool.timeout-seconds"
fi

if [ "$avg_duration" -gt 10000 ]; then
    echo "- Screenshots are taking too long. Check network connectivity."
    echo "- Consider using faster test sites for load testing."
fi

echo ""
echo "🧹 Cleaning up..."
curl -s -X DELETE "$BASE_URL/api/site/test-site" \
    -H "Authorization: $AUTH_KEY" > /dev/null

echo "✅ Load test completed!"
echo "📄 Detailed results: load_test_results.csv" 