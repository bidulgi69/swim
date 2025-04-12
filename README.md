# SWIM: Scalable Weakly-consistent Infection-style Process Group Membership Protocol
A Java-based implementation of the [SWIM protocol](https://www.cs.cornell.edu/projects/Quicksilver/public_pdfs/SWIM.pdf) with [Lifeguard](https://www.hashicorp.com/ko/blog/making-gossip-more-robust-with-lifeguard) enhancements.

---

## Overview

This project implements a lightweight, fault-tolerant cluster membership protocol using:

- **SWIM protocol** (failure detection + gossip dissemination)
- **Lifeguard extensions** (to reduce false positives)
- **Buddy system** (to help suspected nodes recover faster)
- **Incarnation persistence** using timestamp-based conflict resolution
- **Docker Compose + Toxiproxy** for network fault simulation

---

## Features

- Failure detection via ping / ping-req  
- Gossip-based membership updates
- Incarnation versioning with file persistence  
- Lifeguard: delayed suspect, buddy announce, local health multiplier  
- Dockerized test environment ***(/tests)***

---

## Architecture

```text
[Node A] --ping--> [Node B]
       \__ ping-req via C __/

Each node:
  - Periodically pings peers
  - Shares membership updates via gossip
  - Maintains incarnation-based state conflict resolution
```

---

## Testing Scenario

### resurrection test
```shell
chmod +x tests/resurrection/test.sh && ./tests/resurrection/test.sh 
```
- Stops a node
- Waits until it's marked `DEAD`
- Restarts the node (incarnation bump from disk)
- Verifies that it is correctly marked `ALIVE` again

### latency injection test
```shell
chmod +x tests/ping-req/test.sh && ./tests/ping-req/test.sh 
```
- Injects latency toxic on connection node-1 to node-3
- Node-1 asks node-2 to check node-3's ping on its behalf
- Confirmation from node-2 prevents node-3 from being marked as `SUSPICIOUS`
- Verifies that node-3 in node-1's membership is marked as `ALIVE`

### Buddy system test
```shell
chmod +x tests/buddy-system/test.sh && ./tests/buddy-system/test.sh 
```
- Marks node-3 as `SUSPICIOUS` in node-1's membership
- Node-1 spreads gossip that node-3 is suspicious
- When the gossip reaches out node-3, it spreads a new gossip to its buddies that node-3 is alive

---

## References
https://ojt90902.tistory.com/1683<br>
https://www.hashicorp.com/ko/blog/making-gossip-more-robust-with-lifeguard