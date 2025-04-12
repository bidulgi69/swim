#!/bin/bash
set -e

check_state() {
  local expected="$1"
  local actual
  actual=$(curl -s http://localhost:8080/membership \
    | jq -r '.[] | select(.id == "swim-node2") | .state')

  echo "State of node-2: $actual (Expected: $expected)"

  if [[ "$actual" == "$expected" ]]; then
    return 0
  else
    echo "Mismatch {expected: $expected, actual: $actual}"
    return 1
  fi
}

docker-compose up -d --build
sleep 5
check_state "ALIVE"

echo "Shutting down node-2"
docker-compose stop swim-node2

sleep 20

check_state "DEAD"

echo "Restart node-2"
docker-compose start swim-node2

sleep 20

# stale resurrection
check_state ALIVE

# cleanup
docker-compose down --remove-orphans
docker volume rm $(docker volume ls -qf dangling=true)