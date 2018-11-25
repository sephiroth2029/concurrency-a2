# **Concurrency (CSC-564)**
## **Assignment 2**

### **Introduction**
This assignment focuses on distributed systems. It comprises 3 parts:
1. [Vector clocks][1]. In this part, an implementation of the vector clocks algorithm was constructed. The distributed architecture was designed using microservices, which communicate through Netflix's [Eureka][2] server. 
2. [Byzantine generals][30]. In this part, the probabilistic solution for the problem was implemented. Again, the generals are represented as microservices.
3. [Chaos testing and improvement of microservices][37]. In this part, several tools from Netflix were explored and implemented as a first approach to perform evaluations on the final project.

### **Problems**
In this section, the problems, their solutions and the results obtained are described in greater detail.

#### **1. Vector clocks**
##### **Description**
[Lamport's logical clocks][20] ensure that we can identify "happens before" causal relationships.  They don't go so far as to identify which events might be causally concurrent.  [Vector clocks][21] can though!

##### **Implementation**
The environment comprises three separate Java projects, built using Spring Boot and Maven:
1. [The Eureka server][3]. This project is only meant to start Netflix' Eureka server, which is a discovery server for microservices. It allows to start multiple instances and keep track of all of them, while removing any hostname/address dependency from the clients.
2. [The clients][4]. The implementation of the algorithm is contained in this project using microservices. There are four submodules: three for each client and one with the definition of endpoints for message configuration and execution, as well as vector and history retrieval. Each client is an independent microservice which registers itself to the Eureka server and queries it to resolve the other clients while exchanging messages.
3. [The test framework][5]. This project was built in order to ease the execution of tests and the analysis of results. When executed, the path to the test configuration file should be provided. The configuration is in YAML format and it contains the events that will be showcased.

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
Each configuration file was processed and the scenario was configured in each client, specifying who should send a message to whom and what delay from the beginning should be considered. Since the times may vary from process to process, there is a large gap between events (5 seconds). Each time a counter moves, the current state of the vector is preserved in a collection of vectors, denominated *history*. At the end of the execution the history for each client is retrieved and displayed for analysis.

The tests were performed in two major batchs: one to test causality and another to test causally concurrent events.

###### **Causality**
In this test, the files [test1.yml][7], [test2.yml][8] and [test3.yml][9] were configured to represent the events shown the following diagram (taken from [Wikipedia][10]):
![Missing graph][11]

`test1.yml` is configured to represent the events in the diagram, `test2.yml` switches the 3rd and 4th events and `test3.yml` switches the 1st and 2nd with respect to `test2.yml`.

The result of these changes is not seen on the final vectors, as they are identical for every process. However, a history of vectors was implemented to further analyze the intermediate progress. The results are as follows:

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

It is clear that the causality in the intermediate events is affected by the order in which these events are started. In particular, if we look at the fourth set of vectors for `client-a` and `client-c`, we can see that it changes from `test1.yml` to `test2.yml`, impacting the value of the following vector set. Similarly, the third set of vectors for `client-a` and `client-b` change between `test1.yml` and `test2.yml`. However, the final vectors are not affected as all the messages eventually add up to the counters.

Since all of the events were messages being sent from one process to another, there was no room for concurrency and all of the initial events affected the progression of the following messages. In the next batch of tests, internal events were configured to identify concurrent events and see their effect on the intermediate vectors.

###### **Causally concurrent**
In this test, the files [test4.yml][12], [test45.yml][13], [test6.yml][14], [test7.yml][15], [test8.yml][16] and [test9.yml][17] were configured to represent the events shown the following diagram (taken from [this lecture][18]):
![Missing graph][199]

`test4.yml` is configured to represent the previous diagram, and will be used as a baseline for the analysis.

`test5.yml` inverts the order of the first two events. The resulting log shows that the two events don't affect the interaction between them, and because of that the vectors remain the same. Therefore, we can conclude that these events are causally concurrent:

`test4.yml`
```
...
2018-11-15 23:08:48.758  INFO 9940 --- [           main] c.u.c.g.a.v.TestExecution                : client-b final vector: [[0,0],[0,1],[0,2],[2,3],[2,4]]
2018-11-15 23:08:48.783  INFO 9940 --- [           main] c.u.c.g.a.v.TestExecution                : client-a final vector: [[0,0],[1,0],[2,0],[3,0],[4,3]]
```

`test5.yml`
```
...
2018-11-15 23:15:48.648  INFO 13192 --- [           main] c.u.c.g.a.v.TestExecution                : client-b final vector: [[0,0],[0,1],[0,2],[2,3],[2,4]]
2018-11-15 23:15:48.672  INFO 13192 --- [           main] c.u.c.g.a.v.TestExecution                : client-a final vector: [[0,0],[1,0],[2,0],[3,0],[4,3]]
```

For `test6.yml`, the events 3, 4 and 5 changed their order, yielding the different vectors starting at the fourth vector set and affecting the last set.

`test4.yml`
```
...
2018-11-15 23:08:48.758  INFO 9940 --- [           main] c.u.c.g.a.v.TestExecution                : client-b final vector: [[0,0],[0,1],[0,2],[2,3],[2,4]]
2018-11-15 23:08:48.783  INFO 9940 --- [           main] c.u.c.g.a.v.TestExecution                : client-a final vector: [[0,0],[1,0],[2,0],[3,0],[4,3]]
```

`test6.yml`
```
...
2018-11-15 23:23:09.948  INFO 10800 --- [           main] c.u.c.g.a.v.TestExecution                : client-b final vector: [[0,0],[0,1],[0,2],[0,3],[4,4]]
2018-11-15 23:23:09.968  INFO 10800 --- [           main] c.u.c.g.a.v.TestExecution                : client-a final vector: [[0,0],[1,0],[2,0],[3,2],[4,2]]
```

For `test7.yml`, the last two events were inverted. In this case, as in the first test, a change of order on the execution of the events does not affect the final vector. The reason for that is that they are internal events and there is no relationship between them; they are causally concurrent as well:

`test4.yml`
```
...
2018-11-15 23:08:48.758  INFO 9940 --- [           main] c.u.c.g.a.v.TestExecution                : client-b final vector: [[0,0],[0,1],[0,2],[2,3],[2,4]]
2018-11-15 23:08:48.783  INFO 9940 --- [           main] c.u.c.g.a.v.TestExecution                : client-a final vector: [[0,0],[1,0],[2,0],[3,0],[4,3]]
```

`test7.yml`
```
...
2018-11-15 23:27:49.837  INFO 14392 --- [           main] c.u.c.g.a.v.TestExecution                : client-b final vector: [[0,0],[0,1],[0,2],[2,3],[2,4]]
2018-11-15 23:27:49.858  INFO 14392 --- [           main] c.u.c.g.a.v.TestExecution                : client-a final vector: [[0,0],[1,0],[2,0],[3,0],[4,3]]
```

Finally, `test8.yml` and `test9.yml` show what would happen if we included events after the last internal events executed, to confirm the aforementioned concurrency. As expected, the vector history is exactly the same for the three cases:

`test4.yml`
```
...
2018-11-15 23:08:48.758  INFO 9940 --- [           main] c.u.c.g.a.v.TestExecution                : client-b final vector: [[0,0],[0,1],[0,2],[2,3],[2,4]]
2018-11-15 23:08:48.783  INFO 9940 --- [           main] c.u.c.g.a.v.TestExecution                : client-a final vector: [[0,0],[1,0],[2,0],[3,0],[4,3]]
```

`test8.yml`
```
...
2018-11-15 23:32:26.685  INFO 10824 --- [           main] c.u.c.g.a.v.TestExecution                : client-b final vector: [[0,0],[0,1],[0,2],[2,3],[2,4],[2,5],[5,6]]
2018-11-15 23:32:26.701  INFO 10824 --- [           main] c.u.c.g.a.v.TestExecution                : client-a final vector: [[0,0],[1,0],[2,0],[3,0],[4,3],[5,3],[6,6]]
```

`test9.yml`
```
...
2018-11-15 23:33:48.318  INFO 8064 --- [           main] c.u.c.g.a.v.TestExecution                : client-b final vector: [[0,0],[0,1],[0,2],[2,3],[2,4],[2,5],[5,6]]
2018-11-15 23:33:48.338  INFO 8064 --- [           main] c.u.c.g.a.v.TestExecution                : client-a final vector: [[0,0],[1,0],[2,0],[3,0],[4,3],[5,3],[6,6]]
```

#### **2. Byzantine generals**
##### **Description**
The [Byzantine generals problem][22] was defined by Lamport, Shostak and Pease in 1982. A summary explanation of the problem can be found in [YouTube][23]. Lamport also proposes an statistical solution, which was implemented for this part of the assignment.

##### **Implementation**
The solution to the problem was implemented using microservices, one for each general. There is a single project for the n generals, but it is required to start an Eureka server. The server [project][24] for the vector clocks can and was reused to test the generals.

To better understand the [generals' project][25] let us consider the following:

1. [bat files][26] serve as the configuration and execution files. Since the tests were executed on Windows, they are batch files which can be easily converted to a bash file for Linux/Mac. Parameters for each general can be configured in them. General 0 will always be the commander, and once it starts and registers to the Eureka server it will start pinging the other generals until all of them are up. Then it will send the command.
2. As part of the parameters, each general receives a `concurrency.a2.byz.traitor` boolean flag, to indicate if the general is loyal or not. If a general is not loyal, the messages it sends will not be consistent; it will send the original order if the recepient general number is odd and the opposite value if it is even.
3. The number of rounds is specified through the `concurrency.a2.byz.rounds` flag. In Lamport's algorithm the number of rounds should be m + 1 and, as we will see in the results section, this is crucial to actually solve the problem.
4. By default all logs go to `logs/byzantine-generals.log`. However, in order to have one log file per general, it is currently defined for each microservice.
5. Messages written to the logs are kept to a minimum, as  tests with 3 or more traitors produce a large number of messages and logging affects the performance greatly. Still, they can be reenabled by adjusting the logging parameter `logging.level.ca.uvic.concurrency.gmmurguia.a2.byzantinegens` to `DEBUG`.
6. The two most important components of the project are [General.java][27], where most of the algorithm's logic is implemented. and [GeneralController.java][28], which is the component that exposes the endpoint to receive messages.

##### **Results**
Once the `<file>.bat` is executed, each general will be started as an independent process. Since they are independent it is possible to run all of them on the same machine or use several to distribute the generals; the only caveat is that the Eureka server must be visible to all of them and all should register on the same server, so they can discover each other. The following picture illustrates the execution of the batch file:

![Missing graph][299]

###### **One traitor**
In this case the minimum number of generals for the algorithm to work are 7, and it can be solved in two rounds. Below is a summary of the execution of the generals. Notice that the times are not sequencial because this was taken from individual log files.

```
2018-11-21 14:01:44.433  INFO 44536 --- [           main] c.u.c.g.a2.byzantinegens.General         : [General 0] I'm commander, sending order (ATTACK)
2018-11-21 12:46:18.708  INFO 53796 --- [pool-1-thread-1] c.u.c.g.a2.byzantinegens.General         : [General 1] Decided on RETREAT
2018-11-21 12:46:17.882  INFO 79040 --- [pool-1-thread-1] c.u.c.g.a2.byzantinegens.General         : [General 2] Decided on RETREAT
2018-11-21 12:46:18.960  INFO 77236 --- [pool-1-thread-1] c.u.c.g.a2.byzantinegens.General         : [General 3] Decided on RETREAT
2018-11-21 12:46:16.144  INFO 42712 --- [pool-1-thread-1] c.u.c.g.a2.byzantinegens.General         : [General 4] Decided on RETREAT
2018-11-21 12:46:16.903  INFO 64940 --- [pool-1-thread-1] c.u.c.g.a2.byzantinegens.General         : [General 5] Decided on RETREAT
2018-11-21 12:46:16.915  INFO 13764 --- [pool-1-thread-1] c.u.c.g.a2.byzantinegens.General         : [General 6] Decided on RETREAT
```

###### **Three traitors**
The case for two traitors is more challenging, as each general has to detect possible loops in the messages and avoid relaying those messages. However, in order to test if the algorithm would be able to scale up, instead of testing with two traitors, three proved to give better insights:
1. First, it took much longer for each general to converge and come up with a reponse. While testing this part, intermediate messages were printed to the log and they had to be moved to a `DEBUG` log, as the time spent on logging affected greatly the performance and it seemed as if it had fallen on a loop.
2. Second, after the logging was removed, the load to each microservice's web server was so high that it started dropping packages. To solve this, a rudimentary retry approach was implemented, giving the server a chance to finish its current load.

After fixing these problems, the generals converged to the same result. Finally, as an additional test, the rounds were reduced from four to three. The result was that the generals finished, but they didn't agree on the result.

As a final test, a file for 7 generals, 3 traitors and 4 rounds was created. It is interesting to note that for that test the generals agreeded on the result. However, that result is not guaranteed. In this case, the traitors relay half of the messages, increasing the chance of obtaining the same result.

#### **3. Chaos testing and improvement of microservices**
##### **Description**
Chaos testing is a relatively new approach to testing initiated by [Netflix][31] in which failure is introduced to a system to ensure that it will be able to withstand those failures while still providing a good quality level to incoming requests. One of the most popular Chaos Engineering tools is [Chaos Monkey][32]. 

The core idea behind chaos testing is to prove right or wrong a hypothesis regarding the system's stability. If the outcome was the expected result, then the level of confidence on the system increases, and if it fails then actions should be taken to improve the communication channels. Netflix also has developed and open sourced tools to be able to respond to failure. [Hystrix][33] is one of such tools, and it is meant to setup fallback handlers to respond to undesired conditions, such as latency, service errors and service communication problems.

Finally, monitoring applications allows to obtain insights about the production state and behavior of microservices. There are several tools to do this, and Netflix has also open sourced the [Hystrix dashboard][34] to provide real-time statistics of microservices' endpoints.

##### **Implementation**
[Spring Boot][35] is a framework meant to ease the development Java applications, and to simplify the integration of tools and versions in repositories. The microservices for all parts of this assignment were developed using Spring Boot given that many of Netflix's tools already have support in it. In this part, a very basic micoservices [application][38] was built and [Chaos Monkey for Spring Boot][36] was set up to make it fail randomly. A microservices [client][39] was built and a couple of [test cases][40] were written to test iterations on two different client implementations.

The [first][41] of the clients, `TestClient.java`, was built using a standard component to consume web services. During the evaluation of the capabilities of `Chaos Monkey`, an alternative mechanism to build clients declaratively was found. It is through a tool called [Feign][42]. In order to compare the usage and evaluate it, a [feign client][43] was built in the `FeignTestClient.java` class. Both clients were configured with Hystrix, to provide a fallback response in the case that irregular situations happened on the client. The microservices project was configured to provide real time metrics through [micrometer.io][44]. This instrumentation module feeds several visualization tools and, for this implementation, the metrics were manually consumed using [JMX][45].

`Hystrix Dashboard` is another tool that may consume the statistics produced by micrometer.io. The [projects][47] from a [DZone article][46] were cloned and adjusted to evaluate the Dashboard in isolation. That project starts two microservices and exposes real time data through the `Hystrix Dashboard`, but it is necessary to consume the services or the graphs do not represent anything useful. Because of that, the [resulting project][48] was tested through [Gatling][49], a load and performance testing tool. Several requests were sent while the application was being monitored and the resulting graphs were captured.

##### **Results**
Chaos Monkey for Spring Boot turned out to provide more functionality than what was expected. Note that a good response would be "test", while a bad response would be "Fallback" for the Feign client and "Sorry, no String available at the moment" for the standard `RestClient`. The responses were stored in a list and displayed at the end of the test case. Three types of failure were tested:

- **Latency**. The endpoint's response was slowed down. As a result, it was possible to use Hystrix to tweak the client and ensure that, in the case of a slow responses or hanging connections, the end-user still receives a response and is able to continue consuming the application. This configuration can be activated in `application.properties` with this line:
```properties
chaos.monkey.assaults.latency-active=true
chaos.monkey.assaults.latencyRangeStart=4000
chaos.monkey.assaults.latencyRangeEnd=40000
```
The feign client can be configured with a timeout; if a service takes longer than that the fallback response is returned. The relevant configuration is as follows:
```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
```
After executing the test cases it is clear that we obtained fallback responses due to slow server responses:
![Missing graph][50]
```
[test, Fallback, test, test, test, test, test, test, test, test, Fallback, Fallback, test, test, test, test, test, test, test, test, test, test, Fallback, test, test, test, test, test, test, ..., Fallback]
...
[test, test, test, test, test, test, test, test, test, test, Sorry, no String available at the moment., test, test, test, test, Sorry, no String available at the moment., test, Sorry, no String available at the moment., ..., Sorry, no String available at the moment.]
```
- **Server errors**. A Chaos Monkey error (HTTP 500 status) can be returned randomly from the service. In this case Hystrix allows to handle those errors in the same manner as Latency problems, by calling a fallback method. The configuration to create random failure responses is:

```properties
management.endpoint.chaosmonkey.enabled=true
```
And the result was:
```
[test, Fallback, Fallback, test, test, test, test, test, test, Fallback, test, Fallback, Fallback, test, Fallback, Fallback, Fallback, Fallback, test, test, test, test, test, test, test, test, test, test, Fallback, test, test, test, test, test, test, test, test, ..., test]
...
[test, test, test, test, test, test, test, test, test, Sorry, no String available at the moment., test, test, test, test, test, test, test, Sorry, no String available at the moment., test, test, test, test, test, test, test, test, test, Sorry, ..., no String available at the moment.]
```
- **Application shutdown**. It is possible to completely bring down an application through configuration. In this case, enabling this feature was not through a file but through JMX MBeans:
![Missing graph][51].
In this case, the client keeps using its fallback mechanism but no implementation was made to automatically bring up the server again. Due to this, after the sevice was brought down all subsecuent calls received the fallback response:

The next step for this part of the assignment was to monitor the applications. The `sample-spring-hystrix` project was cloned and started to that end. The interaction with the microservices is exposed through a website and through a JSON REST endpoint.

The website is exposed at `http://localhost:8080`:
![Missing graph][52].

The JSON REST endpoint is at `http://localhost:8080/sample-hystrix-aggregate/hello`:
![Missing graph][53].

Once we start the `Hystrix Dashboard` it stays in a load state, until the microservices have received enough requests to have reportable data:
![Missing graph][54].

Since generating traffic by hand is cumbersome, `Gatling` tests were created for [the website][55] and the [REST service][56]. An [HTML report][57] is generated with relevant metrics and statistics about the executed test, such as the following:
![Missing graph][58].

After generating traffic to the microservices' endpoints, the `Hystrix Dashboard` shows in real time statistics about the Hystrix commands:
![Missing graph][59].

### **Conclusions**
Distributed systems have evolved as a necessity of the industry and the target has been to provide users with a seamless experience. Consistency and reliability are top concerns for distributed systems' designers and developers. Being able to order events and to produce results despite of failures make vector clocks and concensus algorithms as relevant as they were over 30 years ago.

On the other hand, testing these uncertain environments and achieving an acceptable level of confidence in their robustness has gained the attention of top companies, such as Netflix. Fortunately, access to their tools and frameworks allows the whole community to benefit from and improve those components. In this assignment a handful of them were explored under the belief that testing and monitoring are essential parts of software engineering.

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
[20]: https://amturing.acm.org/p558-lamport.pdf
[21]: http://zoo.cs.yale.edu/classes/cs426/2012/lab/bib/fidge88timestamps.pdf
[22]: https://people.eecs.berkeley.edu/~luca/cs174/byzantine.pdf
[23]: https://www.youtube.com/watch?v=_MwqAaVweJ8
[24]: https://github.com/sephiroth2029/concurrency-a2/tree/master/part1/vector-clocks
[25]: https://github.com/sephiroth2029/concurrency-a2/tree/master/part2/byzantine-gens
[26]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part2/byzantine-gens/
[27]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part2/byzantine-gens/src/main/java/ca/uvic/concurrency/gmmurguia/a2/byzantinegens/General.java
[28]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part2/byzantine-gens/src/main/java/ca/uvic/concurrency/gmmurguia/a2/byzantinegens/GeneralController.java
[29]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part2/diagrams/one_traito_execution.PNG?raw=true
[30]: https://github.com/sephiroth2029/concurrency-a2/tree/master/part2
[31]: https://web.archive.org/web/20141008152549/http://techblog.netflix.com/2014/09/introducing-chaos-engineering.html
[32]: https://github.com/Netflix/SimianArmy/wiki/Chaos-Monkey
[33]: https://github.com/Netflix/Hystrix
[34]: https://github.com/Netflix-Skunkworks/hystrix-dashboard
[35]: https://spring.io/projects/spring-boot
[36]: https://codecentric.github.io/chaos-monkey-spring-boot/
[37]: https://github.com/sephiroth2029/concurrency-a2/tree/master/part3
[38]: https://github.com/sephiroth2029/concurrency-a2/tree/master/part3/demo
[39]: https://github.com/sephiroth2029/concurrency-a2/tree/master/part3/chaos-monkey-poc
[40]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part3/chaos-monkey-poc/src/test/java/ca/uvic/concurrency/gmmurguia/a2/chaosmonkeypoc/ChaosMonkeyPocApplicationTests.java
[41]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part3/chaos-monkey-poc/src/main/java/ca/uvic/concurrency/gmmurguia/a2/chaosmonkeypoc/TestClient.java
[42]: https://github.com/OpenFeign/feign
[43]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part3/chaos-monkey-poc/src/main/java/ca/uvic/concurrency/gmmurguia/a2/chaosmonkeypoc/FeignTestClient.java
[44]: https://micrometer.io/
[45]: https://www.eclipse.org/jetty/documentation/9.4.x/jmx-chapter.html
[46]: https://dzone.com/articles/spring-cloud-with-turbine
[47]: https://github.com/bijukunjummen/sample-spring-hystrix
[48]: https://github.com/sephiroth2029/concurrency-a2/tree/master/part3/sample-spring-hystrix
[49]: https://gatling.io/
[50]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part3/diagrams/chaosmonkey_latency.PNG?raw=true
[51]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part3/diagrams/chaosmonkey_killapp.PNG?raw=true
[52]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part3/diagrams/hystrix_gui.PNG?raw=true
[53]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part3/diagrams/hystrix_helloworld.PNG?raw=true
[54]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part3/diagrams/hystrix_helloworld.PNG?raw=true
[55]: https://github.com/sephiroth2029/concurrency-a2/tree/master/part3/gatling/user-files/simulations
[56]: https://github.com/sephiroth2029/concurrency-a2/tree/master/part3/gatling/user-files/simulations2
[57]: https://github.com/sephiroth2029/concurrency-a2/tree/master/part3/gatling/results/recordedsimulation-20181124045656442
[58]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part3/diagrams/gatling.PNG?raw=true
[59]: https://github.com/sephiroth2029/concurrency-a2/blob/master/part3/diagrams/HystrixDashbard_running.PNG?raw=true
