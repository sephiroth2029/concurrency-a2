# **Concurrency (CSC-564)**
## **Assignment 2**

### **Introduction**
This assignment focuses on distributed systems. It comprises 3 parts:
1. [Vector clocks][1]. In this part, an implementation of the vector clocks algorithm was constructed. The distributed architecture was designed using microservices, which communicate through Netflix's [Eureka][2] server. 
2. Byzantine generals. In this part, the probabilistic solution for the problem was implemented. Again, the generals are represented as microservices.
3. Chaos Monkey. Finally, Netflix' testing framework was explored, as a first approach to test the final project.

### **Problems**
In this section, the problems, their solutions and the results obtained are described in greater detail.

#### **1. Vector clocks**
##### **Description**
Lamport's logical clocks ensure that we can identify "happens before" causal relationships.  They don't go so far as to identify which events might be causally concurrent.  Vector clocks can though!

##### **Implementation**
The environment comprises three separate Java projects, built using Spring Boot and Maven:
1. [The Eureka server][3]. This project is only meant to start Netflix' Eureka server, which is a discovery server for microservices. It allows to start multiple instances and keep track of all of them, while removing any hostname/addres dependency from the clients.
2. [The clients][4]. The implementation of the algorithm is contained in this project. There are four submodules: three for each client and one with the definition of endpoints for message configuration and execution, as well as vector and history retrieval. Each client is an independent microservice which registers itself to the Eureka server and queries it to resolve the other clients while exchanging messages.
3. [The test framework][5]. This project was built in order to ease the execution of tests and the analysis of results. When executed, the path to the test configuration file should be provided. The configuration is in YAML format and it contains the events that will flow through the system.

The test configuration files are found [here][6]. Each file contains a list of events with following structure:

```
events:
  - source: <source-process-id>
    dest: <destination-process-id>
    start: <execution-start-time>
```

Where:

- **source-process-id** is the ID of the process/microservice which will send the message.
- **destination-process-id** is the ID of the recipient of the message. If the destination is specified as `self`, it means that this event only occurs inside the source process.
- **execution-start-time** the time at which the message will activate. Inside the clients, when an event is configured, the initial state of the vector is stored and after `execution-start-time` seconds the message is sent.

##### **Results**
The tests were performed in two major batchs: one to test causality and another to test causally concurrent events.

###### **Causality**
In this test, the files [test1.yml][7], [test2.yml][8] and [test3.yml][9] were configured to represent the events shown the following diagram (taken from [Wikipedia][10]):
![Missing graph][11]

`test1.yml` represents this diagram

[1]: https://github.com/sephiroth2029/concurrency-a2/tree/master/part1
[2]: https://github.com/Netflix/eureka/wiki/Eureka-at-a-glance
[3]: https://github.com/sephiroth2029/concurrency-a2/tree/master/part1/vector-clocks
[4]: https://github.com/sephiroth2029/concurrency-a2/tree/master/part1/vector-clocks-clients
[5]: https://github.com/sephiroth2029/concurrency-a2/tree/master/part1/vector-clocks-tester
[6]: https://github.com/sephiroth2029/concurrency-a2/tree/master/part1/vector-clocks-tester/src/main/resources
[7]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part1/vector-clocks-tester/src/main/resources/test1.yml
[8]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part1/vector-clocks-tester/src/main/resources/test2.yml
[9]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part1/vector-clocks-tester/src/main/resources/test3.yml
[10]: https://en.wikipedia.org/wiki/Vector_clock
[11]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part1/diagrams/725px-Vector_Clock.svg.png?raw=true
