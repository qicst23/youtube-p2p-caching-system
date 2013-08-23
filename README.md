Youtube P2P Caching System
==========================

A peer-to-peer decentralized caching system storing RESTful YouTube search results in FreePastry-based distributed hash table (DHT). Java.

## _Skill_
    Java, Servlet, FreePastry Distributed Hash Table, Web Services(REST, SOAP), Bekeley DB

## _Contribution_
1. Built a peer-to-peer decentralized caching system storing RESTful YouTube search results in FreePastry-based distributed hash table (DHT).
2. Built a web services based web application that queries and receives REST(JSON)/SOAP messages between client and caching system.

## _Deployment_ 
To launch P2P ring, you need to use:
        
        java -cp [lib files] [P2PCache location]
        [the port number on the local machine to which the Pastry node should bind]
        [the IP address of the Pastry bootstrap node]
        [the port number of the Pastry bootstrap node]
        [the port number to which the daemon should bind]
        [the path to the BerkeleyDB database]
        [max maximum storage size]?
        [content expiration time]?

        For example: 
        java -cp youtube.jar:target/WE:target/WEB-INF/lib/* edu/upenn/cis455/youtube/P2PCache 9001 158.130.213.1 9001 10001 /Users/Alantyy/Documents/database 10 1000
        For example:
        java -cp youtube.jar:target/WEB-INF/lib/* edu/upenn/cis455/youtube/P2PCache 9001 10.12.231.124 9001 10001 /Users/Alantyy/Desktop/database 10 1000
 
## _Files_
1. Main source files are located in `src/edu/upenn/cis455`.
2. In `/storage` folder: `DatabaseUtil.java` is for Berkeley DB DAO functions, `P2PNode.java` stores information 
about PastryNode and `YouTubeResult.java` for YouTube reults.
3. In `/youtube` folder: `P2P Cache` is the main class to launch P2P ring and server, `YouTubeSearch.java` is the
main class to launch servlet web application, `NodeFactory.java` encapsulates functions to create P2P nodes and ring,
`P2PApplication.java` is used for routing - forwarding requests to specific nodes, `YouTubeClient.java` is used to search YouTube using google API, 
`P2PDaemonServer.java` is the same software running on
every P2PNode used to listen to requests from client.
4. Links: [view specification](http://www.cis.upenn.edu/~cis455/handouts/Homework-3.pdf).
