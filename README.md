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
Each configuration file was processed and the scenario was configured in each client, specifying who should send a message to whom and what delay from the beginning should be considered. Since the times may vary from process to process, there is a large gap between events (5 seconds). Each time a counter moves, the current state of the vector is preserved in a history. At the end of the execution the history for each client is retrieved and displayed for analysis.

The tests were performed in two major batchs: one to test causality and another to test causally concurrent events.

###### **Causality**
In this test, the files [test1.yml][7], [test2.yml][8] and [test3.yml][9] were configured to represent the events shown the following diagram (taken from [Wikipedia][10]):
![Missing graph][11]

`test1.yml` is configured to represent this diagram, `test2.yml` switches the 3rd and 4th events and `test3.yml` switches the 1st and 2nd with respect to `test2.yml`.

The result of this changes is not seen on the final vectors, as they are identical for every process. However, a history of vectors was implemented to further analyze the intermediate progress. The results are as follows:

`test1.yml`:
```
...
2018-11-15 22:36:20.125  INFO 19760 --- [           main] c.u.c.g.a.v.TestExecution                : client-c final vector: [[0,0,0],[0,0,1],[0,0,2],[0,3,3],[0,3,4],[2,5,5]]
2018-11-15 22:36:20.142  INFO 19760 --- [           main] c.u.c.g.a.v.TestExecution                : client-b final vector: [[0,0,0],[0,1,0],[0,2,1],[0,3,1],[0,4,1],[2,5,1]]
2018-11-15 22:36:20.160  INFO 19760 --- [           main] c.u.c.g.a.v.TestExecution                : client-a final vector: [[0,0,0],[1,0,0],[2,2,1],[3,3,3],[4,5,5]]
```

`test2.yml`:
```
...
2018-11-15 22:32:31.303  INFO 6920 --- [           main] c.u.c.g.a.v.TestExecution                : client-c final vector: [[0,0,0],[0,0,1],[0,0,2],[2,4,3],[2,4,4],[2,5,5]]
2018-11-15 22:32:31.332  INFO 6920 --- [           main] c.u.c.g.a.v.TestExecution                : client-b final vector: [[0,0,0],[0,1,0],[0,2,1],[0,3,1],[2,4,1],[2,5,1]]
2018-11-15 22:32:31.346  INFO 6920 --- [           main] c.u.c.g.a.v.TestExecution                : client-a final vector: [[0,0,0],[1,0,0],[2,2,1],[3,4,3],[4,5,5]]
```

`test3.yml`:
```
...
2018-11-15 22:28:15.115  INFO 18536 --- [           main] c.u.c.g.a.v.TestExecution                : client-c final vector: [[0,0,0],[0,0,1],[0,0,2],[2,4,3],[2,4,4],[2,5,5]]
2018-11-15 22:28:15.133  INFO 18536 --- [           main] c.u.c.g.a.v.TestExecution                : client-b final vector: [[0,0],[0,1],[0,2],[0,3,1],[2,4,1],[2,5,1]]
2018-11-15 22:28:15.160  INFO 18536 --- [           main] c.u.c.g.a.v.TestExecution                : client-a final vector: [[0],[1],[2,1],[3,4,3],[4,5,5]]
```

The vectors contain the counter at the matching index according to their position. That is, all indexes 0 contain the counters for `client-a`, indexes 1 for `client-b` and 2 for `client-c`. The vector `[2,4,1]` should be read as "`client-a`'s counter is 2, `client-b`'s counter is 4 and `client-c`'s counter is 1". 

It is noticeable that the causality in the intermediate events is affected by the order in which these events are started. In particular, if we look at the fourth set of vectors for `client-a` and `client-c`, we can see that it changes from `test1.yml` to `test2.yml`, impacting the value of the following vector set. Similarly, the third set of vectors for `client-a` and `client-b` change between `test1.yml` and `test2.yml`. However, the final vectors are not affected as all the messages eventually add up to the counters.

Since all of the events were messages being sent from one process to another, there was no room for concurrency and all of the initial events affected the progression of the following messages. In the next batch of tests, internal events were configured to identify concurrent events and see their effect on the intermediate vectors.

###### **Causally concurrent**
In this test, the files [test4.yml][12], [test45.yml][13], [test6.yml][14], [test7.yml][15], [test8.yml][16] and [test9.yml][17] were configured to represent the events shown the following diagram (taken from [this lecture][18]):
![Missing graph][19]


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
[12]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part1/vector-clocks-tester/src/main/resources/test4.yml
[13]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part1/vector-clocks-tester/src/main/resources/test5.yml
[14]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part1/vector-clocks-tester/src/main/resources/test6.yml
[15]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part1/vector-clocks-tester/src/main/resources/test7.yml
[16]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part1/vector-clocks-tester/src/main/resources/test8.yml
[17]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part1/vector-clocks-tester/src/main/resources/test9.yml
[18]: https://www.isical.ac.in/~ansuman/dist_sys/Lecture1.pdf
[19]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part1/diagrams/Concurrent.PNG?raw=true
