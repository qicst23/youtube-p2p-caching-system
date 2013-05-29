Youtube P2P Caching System
==========================

A peer-to-peer decentralized caching system storing RESTful YouTube search results in FreePastry-based distributed hash table (DHT).

## _Skill_
    Java, Servlet, FreePastry Distributed Hash Table, Web Services(REST, SOAP), Bekeley DB

## _Contribution_
1. Built a peer-to-peer decentralized caching system storing RESTful YouTube search results in FreePastry-based distributed hash table (DHT).
2. Built a web services based web application that queries and receives REST(JSON)/SOAP messages between client and caching system.

## _Instruction_
1. Main source files are located in `src/edu/upenn/cis455`.
2. In `/storage` folder, `DatabaseUtil.java` is for Berkeley DB DAO functions, `P2PNode.java` stores information about PastryNode and `YouTubeResult.java` for YouTube reults.
3. In `/youtube` folder: `P2P Cache` is the main class to launch P2P ring, and server, `NodeFactory.java` encapsulates functions to create P2P nodes and ring, `P2PApplication.java` is used for routing - forward requests to specific nodes, `YouTubeClient.java` is used to search YouTube using google API, 
`P2PDaemonServer.java` is the same software running on every P2PNode used to listen to requests from client
4. Links: [view specification](http://www.cis.upenn.edu/~cis455/handouts/Homework-3.pdf)
