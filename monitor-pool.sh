#!/bin/bash

# SlackShot Pool Monitor
# Monitors WebDriver pool status in real time

BASE_URL="http://localhost:3030"
AUTH_KEY="default-auth-key"
INTERVAL=5  # Seconds between updates

echo "📊 SlackShot WebDriver Pool Monitor"
echo "===================================="
echo "Monitoring pool every ${INTERVAL} seconds..."
echo "Press Ctrl+C to stop"
echo ""

# Function to get pool stats
get_pool_stats() {
    response=$(curl -s -X GET "$BASE_URL/api/webdriver/stats" \
        -H "Authorization: $AUTH_KEY")
    echo "$response"
}

# Function to display stats in tabular format
display_stats() {
    local stats=$1
    local timestamp=$(date '+%H:%M:%S')
    
    # Extract values using jq if available, otherwise with grep/sed
    if command -v jq >/dev/null 2>&1; then
        total=$(echo "$stats" | jq -r '.totalDrivers // 0')
        active=$(echo "$stats" | jq -r '.activeDrivers // 0')
        available=$(echo "$stats" | jq -r '.availableDrivers // 0')
        max=$(echo "$stats" | jq -r '.maxPoolSize // 0')
        utilization=$(echo "$stats" | jq -r '.utilizationPercentage // 0')
    else
        # Fallback without jq
        total=$(echo "$stats" | grep -o '"totalDrivers":[0-9]*' | cut -d: -f2)
        active=$(echo "$stats" | grep -o '"activeDrivers":[0-9]*' | cut -d: -f2)
        available=$(echo "$stats" | grep -o '"availableDrivers":[0-9]*' | cut -d: -f2)
        max=$(echo "$stats" | grep -o '"maxPoolSize":[0-9]*' | cut -d: -f2)
        utilization=$(echo "$stats" | grep -o '"utilizationPercentage":[0-9]*' | cut -d: -f2)
    fi
    
    # Display in tabular format
    printf "%-8s | %-6s | %-6s | %-9s | %-6s | %-15s\n" \
        "Time" "Total" "Active" "Available" "Max" "Utilization"
    printf "%-8s-|-%-6s-|-%-6s-|-%-9s-|-%-6s-|-%-15s\n" \
        "--------" "------" "------" "---------" "------" "---------------"
    printf "%-8s | %-6s | %-6s | %-9s | %-6s | %-15s%%\n" \
        "$timestamp" "$total" "$active" "$available" "$max" "$utilization"
    
    # Display utilization progress bar
    local bars=$((utilization / 5))
    local spaces=$((20 - bars))
    printf "Utilization: ["
    for ((i=0; i<bars; i++)); do printf "#"; done
    for ((i=0; i<spaces; i++)); do printf " "; done
    printf "] %d%%\n" "$utilization"
    
    # Display alerts
    if [ "$utilization" -gt 80 ]; then
        echo "⚠️  High pool utilization!"
    fi
    
    if [ "$available" -eq 0 ] && [ "$active" -gt 0 ]; then
        echo "🚨 No available drivers in pool!"
    fi
    
    if [ "$total" -eq "$max" ] && [ "$utilization" -eq 100 ]; then
        echo "🔥 Pool at maximum capacity!"
    fi
}

# Function to show performance statistics
show_performance_tips() {
    echo ""
    echo "💡 Performance Tips:"
    echo "==================="
    
    local stats=$(get_pool_stats)
    
    if command -v jq >/dev/null 2>&1; then
        utilization=$(echo "$stats" | jq -r '.utilizationPercentage // 0')
        max=$(echo "$stats" | jq -r '.maxPoolSize // 0')
    else
        utilization=$(echo "$stats" | grep -o '"utilizationPercentage":[0-9]*' | cut -d: -f2)
        max=$(echo "$stats" | grep -o '"maxPoolSize":[0-9]*' | cut -d: -f2)
    fi
    
    if [ "$utilization" -gt 80 ]; then
        echo "• Consider increasing webdriver.pool.max-size"
        echo "• Current max: $max, try: $((max + 2))"
    fi
    
    if [ "$utilization" -lt 20 ]; then
        echo "• Pool is underutilized, consider reducing max-size"
        echo "• Current max: $max, try: $((max - 1))"
    fi
    
    echo "• Monitor for memory leaks with high active driver count"
    echo "• Check logs for WebDriver creation/cleanup issues"
}

# Show current configuration
echo "🔧 Current Configuration:"
echo "========================="
if [ -f "src/main/resources/application.yml" ]; then
    echo "Pool settings from application.yml:"
    grep -A 5 "webdriver:" src/main/resources/application.yml | grep -E "(core-size|max-size|queue-capacity)" || echo "Using default values"
else
    echo "Using default pool configuration"
fi
echo ""

# Show table header
printf "%-8s | %-6s | %-6s | %-9s | %-6s | %-15s\n" \
    "Time" "Total" "Active" "Available" "Max" "Utilization"
printf "%-8s-|-%-6s-|-%-6s-|-%-9s-|-%-6s-|-%-15s\n" \
    "--------" "------" "------" "---------" "------" "---------------"

# Main monitoring loop
while true; do
    stats=$(get_pool_stats)
    
    if [ $? -eq 0 ] && [ -n "$stats" ]; then
        display_stats "$stats"
    else
        echo "$(date '+%H:%M:%S') | Error getting pool stats"
    fi
    
    # Show tips every 30 seconds
    if [ $((SECONDS % 30)) -eq 0 ]; then
        show_performance_tips
    fi
    
    sleep $INTERVAL
done 