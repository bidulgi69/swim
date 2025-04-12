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

## References
https://ojt90902.tistory.com/1683<br>
https://www.hashicorp.com/ko/blog/making-gossip-more-robust-with-lifeguard