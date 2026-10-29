#!/usr/bin/env bash
# Fires N requests with C concurrent clients at GET /thread and reports the total time.
# Uses `hey` (https://github.com/rakyll/hey) when installed, otherwise falls back to curl + xargs.
#
# Usage: scripts/load-test.sh [requests] [concurrency] [url]
# Start the app first, e.g.: ./mvnw spring-boot:run -Dspring-boot.run.profiles=platform
set -euo pipefail

REQUESTS="${1:-2000}"
CONCURRENCY="${2:-1000}"
URL="${3:-http://localhost:8080/thread}"

echo "Requests: $REQUESTS  Concurrency: $CONCURRENCY  URL: $URL"

if command -v hey >/dev/null 2>&1; then
  hey -n "$REQUESTS" -c "$CONCURRENCY" -t 120 "$URL"
  exit 0
fi

echo "hey not found, using curl + xargs (install hey for latency percentiles)"
CODES="$(mktemp)"
start=$(date +%s)
seq "$REQUESTS" | xargs -P "$CONCURRENCY" -I{} \
  curl -s -o /dev/null -w "%{http_code}\n" --max-time 120 "$URL" > "$CODES"
end=$(date +%s)

ok=$(grep -c '^200$' "$CODES" || true)
echo "Total time: $((end - start)) s   HTTP 200: $ok / $REQUESTS"
