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
docker-compose -f docker-compose.toxic.yml build

echo "Running toxiproxy"
docker-compose -f docker-compose.toxic.yml up -d toxiproxy
sleep 5

echo "Add proxy on node-3"
curl -X POST http://localhost:8474/proxies -d '{
  "name": "to-node3",
  "listen": "0.0.0.0:18080",
  "upstream": "swim-node3:8080"
}'

printf "\nRunning cluster nodes..."
docker-compose -f docker-compose.toxic.yml up -d
sleep 5

check_state 8080 "ALIVE"

echo "Injecting latency on node-3"
curl -X POST http://localhost:8474/proxies/to-node3/toxics -d '{
  "name": "latency",
  "type": "latency",
  "attributes": {
    "latency": 10000
  }
}'
printf "\n"

sleep 20

# node-2 guarantees node-3 is alive
check_state 8080 "ALIVE"
check_state 8081 "ALIVE"

echo "Delete toxic on node-3"
curl -X DELETE 'http://localhost:8474/proxies/to-node3/toxics/latency'

sleep 20
# node-1 knows node-3 is alive
check_state 8080 "ALIVE"

# cleanup
docker-compose -f docker-compose.toxic.yml down --remove-orphans
docker volume rm $(docker volume ls -qf dangling=true)