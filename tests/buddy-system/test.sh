#!/bin/bash
set -e

check_state() {
  local expected="$2"
  local actual
  actual=$(curl -s http://localhost:$1/membership \
    | jq -r '.[] | select(.id == "swim-node3") | .state')

  echo "State of node-3: $actual (Expected: $expected)"

  if [[ "$actual" == "$expected" ]]; then
    return 0
  else
    echo "Mismatch {expected: $expected, actual: $actual}"
    return 1
  fi
}

./gradlew clean
./gradlew build
docker-compose build

printf "\nRunning cluster nodes..."
docker-compose up -d
sleep 5

check_state 8080 "ALIVE"

echo "Update state of node-3 in node-1's membership"
curl -X PATCH "http://localhost:8080/suspicion?id=swim-node3"
printf "\n"

# buddy-system
# node-3 tells its buddies that I'm still alive
sleep 20

# verify other nodes(1, 2) recognize node-3 is alive
check_state 8080 "ALIVE"
check_state 8081 "ALIVE"

# cleanup
docker-compose down --remove-orphans
docker volume rm $(docker volume ls -qf dangling=true)