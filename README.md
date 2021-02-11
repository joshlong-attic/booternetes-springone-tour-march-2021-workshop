# The Cloud Native Java Workshop for SpringOne Tour 2021 

> I do not like work even when someone else is doing it. ― Mark Twain

We're with Mr. Twain on this one. we loathe work, especially undifferentiated work - work that you have to do but that doesn't directly contribute to the success of your project, but getting to production today means doing more of it than ever. 

Cloud native computing refers not to any one single technology but more to an approach that optimizes for frequent and fast releases and speed of iteration. Microservices are a part of a broad continiium of technical approaches that, taken together, improve an organization's ability to release new software consistently and frequently. 

Microservices are all about optimizing for release velocity. The faster an organization's velocity the faster it can get working software into the hands of its users where it may delight them. the faster an organization;s velocity the faster it can learn from the experience and  improve in response. There are lots of work queues for each new release to production - things that must be done and that take wall clock take time. Some things - like integration and integration testing -  must be done in a serialized fashion, after all contributions have been made to a codebase, while other things may be done in parallel. The smaller the size of the codebase, the more quickly all the serialized work may be finished. The goal is to do as much work in parallel and to reduce the amount of serialized work, to reduce wall clock time between releases. Imrove these two things  - parallel and serial work -  and the wall time from concept to customer decreases significantly. It's no wonder microservices are popular! _Vroom vroom_.

But theyre not without cost. 

In the world of microservices, the rare tedium of addresssing non functional requirements like security, rate limiting, observability, routing, virtual machines and cloud infrastructure become an ongoing struggle that bedevil every new microservice. 

Microservices also introduce a lot of the complexity implied in building any distributed system. Things will fail in production. You'll need a toolkit that supports building responsive, efficient and scalable services. 

In this workshop, you're goign to run through a number of different technologies that let us have our cake and eat it too: you'll embrace microservices, and hopefully make a huge first step in the right direction towards production, and you'll do so in a way that pays down technical debt.

## Spring Boot: the Service Chassis 

Youre going to build three microservices: two APIs and one API gateway. The two APIs will be technically very similar to each other. One microservice, the `customers` application, manages `Customer` data. The other microservice, the `orders` application, manages `Order` data. 

NOTE: There's a strong case to be made that `order` data is part of the `customer` aggregate, but I always  like to imagine that something like an `order` would endure even if the `customer` were to somehow delete their records and orphan the `orders` because of revenue recognition and fiscal reporting requirements. It would make more sense to tombstone the `order`, wouldn't it? This thinking informs why we've teased this domain into two different microservices. 

Through consistency comes velocity. We'll use Spring Boot to code-generate a brand new Java based application with consistent defaults and eaasy-to-override features. Spring Boot is an opinionated approach to the Java ecosystem. You  opt-in to more automatic configuration ("auto-config") by adding so-called "starter" dependencies to the build. The mere presence of these starter dependencies introduces convenient, sensibly defaulted, behvior to an aplication. 

Chris Richardson coined the pattern _microservice chassis_, whihc basically describes an opionated, automatic framework like Spring Boot whose purpose is to reduce cross cutting concersn for each new application. 

## The Customer Service 

Let's stand up a simple service to handle `Customer` data.  

### The Build


We can generate a new project on the Spring INitializr. Specify an `Artifact ID` (`customers`, perhaps?) and then be sure to select `R2DBC`, `Wavefront`, `Reactive Web`, `Actuator`, `Lombok`, `H2`, and specify `Java 11`. Click `Generate`. You'll now have a `.zip` file that you can unzip and import into your IDE. 

<!-- i wasnt sure about this part. normally id direct people to start.spring.io and have them incept the project there. but i guess in this case were giving them a prepopulated git directory? so maybe this next few paragraphs are more viable than the last one? -->

We'll need to make changes to the Apache Maven build. Here's what it should look like. 

// include: code/orders/pom.xml 

Note that this build includes dependencies for   `R2DBC`, the  `Wavefront` observability platform, `Reactive Web`, `Actuator`, `Lombok`, the `H2`, and `Java 11`.




### The Java Code 

We'll need to make changes to the Java code. There are a few interesting players here. 

The application is a Spring Boot application, so well need the entry point class.

// include: code/orders/src/main/java/com/example/orders/OrdersApplication.java

We're going to read and write data to a sQL database table called `orders`. We'll need an entity class.

// include: code/orders/src/main/java/com/example/orders/Order.java

And we'll need a Spring Data repository.

// include: code/orders/src/main/java/com/example/orders/OrderRepository.java

We're going to read and write data to a sQL database table called `orders`. The H2 SQL database is an embedded, inmemory SQL database that will lose all of its state on every restart.  We'll need to initialize it. 

// include: code/customers/src/main/java/com/example/customers/CustomersListener.java

And, fianlly, we want to export an HTTP endpooint, `/customers`. 

// include: code/customers/src/main/java/com/example/customers/CustomersRestController.java

### The Configuration 

There are some things, like the port, and the logical name, that change from one service to another. We can spell those values out in properties in the application's `application.properties`. 

// include: code/customers/src/main/resource/application.properties 
// todo how do we add only two properties from that file, `server.port`, and `spring.application.name`. We'll add all the others later.

### Go Time 

Let's test it all out. Go to the root of the `custoemrs` code and run: 

```shell
mvn clean spring-boot:run 
```

Use the `curl` CLI to invoke the `/customers` HTTP endpoint. 

```shell
curl http://localhost:8080/customers
```

## Reactive Programming 

This application uses reactive programming, which you may note from the `Flux<T>`, `Mono<T>` and `Publisher<T>` types strewn about the codebase. These types are a sort of inverted `Collection<T>` where, instead of pulling data out of the collection to _pull_ the data out of it when you want it, the data is _pushed_ to you when the data is ready. This inverted approach means that Spring Webflux, the reactive web framweokr that we're using, does not need to wait for an asynchronous value of stream of values to resolve. It can ask for it and then carry on doing other work until the results arrive. This means that no thread is ever parked - _idle_ -  waiting for data to arrive, which means the runtime can repurpose those threads to acheive much better scalability. In the cloud, scalability means less hardware being used to do the same job, which translates into reduced data center spend. Scalability is a _good thing_ (TM). 

Reactive programming also gives us two other major benefits: ease of composition and robustness. You see, reactive programming forces us to think of everything in terms of the Reacttive Streams types like `Publisher<T>`. The [Reactor Project](https://projectreactor.io/) builds upon the Reactive Streams specifiation and provides two  or its specializations `Flux<T>` (a container type for `0..N` values of type `T`) and `Mono<T>` (a container type for at-most one value). All reactive Spring APIs use these types. Types from `org.reactivestreams.*` are the Reactive Streams specififcation. In the Reactive Streams specification, a reactive stream `Publisher<T>` publishes data, asynchronously. A `Subscriber<T>` consumes data from a `Publisher<T>`. A `Subscriber<T>` may request more   data, or cancel the data altogether, from  the `Publisher<T>` by using the `Subscription<T>` 

The code showcases a number of interetin elemetns. `Customer` is an entity object that maps to data read from the in-memory SQL database, H2. The `CustomerRepository` is a Spring Data repository that allows us to read and write entity data. `CustomersListener` installs some sample data into the database when the application starts up. `CustomerRestController` describes an HTTP endpoint, `/customers`. 

All the code that we'll look at today assumes the use of Reactive Applications. 

## RSocket 

We love HTTP, broadly, and the REST constraint on HTTP, specifically, as much as the next cloud native, but it's definitely not the only game in town when it comes to high speed, low-latency, highly scalable, intraservice communication. There are, among many alternatives like GraphQL, GRPC, and - our perennial favorite - RSocket. RSocket is a binary protocol that reifies the concepts of Reactive Streams in the wire protocol. RSocket  understands supports key components of reactive programming, including _backpressure_, and has ways to communicate that information on the wire itself. The protocol is of course platofmr, language and payload agnostic. There is a fantasic Java client, written on top of the Reative Streams specification and Project Reactor, that we could use independent of Spring, if we were so inclined. But, as luck woud have it, spring already integrates RSocket and provides a component model that makes trivial the work of standing up an Rsovket based service. 






## The Orders Service 


Let's stand up a simple service to handle `Order` data.  

<!-- should we include the discusion around the bounded-ness of the order data to the customer aggregate? -->

### The Build

We can generate a new project on the Spring INitializr. Specify an `Artifact ID`  (`orders`, perhaps?) and then be sure to select `R2DBC`, `Wavefront`, `RSocket`, `Actuator`, `Lombok`, `H2`, and specify `Java 11`. Click `Generate`. You'll now have a `.zip` file that you can unzip and import into your IDE. 

<!-- i wasnt sure about this part. normally id direct people to start.spring.io and have them incept the project there. but i guess in this case were giving them a prepopulated git directory? so maybe this next few paragraphs are more viable than the last one? -->

We'll need to make changes to the Apache Maven build. Here's what it should look like. 

// include: code/orders/pom.xml 

Note that this build includes dependencies for   `R2DBC`, the `Wavefront` observability platform, `RSocket`, `Actuator`, `Lombok`,   `H2`, and `Java 11`. 

### The Java Code 

We'll need to make changes to the Java code. There are a few interesting players here. 

The application is a Spring Boot application, so well need the entry point class.

// include: code/orders/src/main/java/com/example/orders/OrdersApplication.java


We're going to read and write data to a sQL database table called `orders`. We'll need an entity class.

// include: code/orders/src/main/java/com/example/orders/Order.java

And we'll need a Spring Data repository.

// include: code/orders/src/main/java/com/example/orders/OrderRepository.java

We're going to read and write data to a sQL database table called `orders`. The H2 SQL database is an embedded, inmemory SQL database that will lose all of its state on every restart. We'll need to initialize it. 

// include: code/orders/src/main/java/com/example/orders/OrdersListener.java

And, fianlly, we want to export an HTTP endpooint, `/orders`. 

// include: code/orders/src/main/java/com/example/orders/OrdersRestController.java

### The Configuration 

There are some things, like the port, and the logical name, that change from one service to another. We can spell those values out in properties in the application's `application.properties`. 

// include: code/orders/src/main/resource/application.properties 
// todo how do we add only two properties from that file, `server.port`, and `spring.application.name`. We'll add all the others later.

### Go Time 

Let's test it all out. Go to the root of the `orders` code and run: 

```shell
mvn clean spring-boot:run 
```

Use the [`rsc` CLI](https://github.com/making/rsc) to invoke the `orders.{customerId}` RSocket endpoint.

```shell
rsc tcp://localhost:8181 --stream  -r orders.3 
```

## Living on the Edge 

At this point we've got two microservices both ready to run. Ensure that they're both running. We're oging to need them running in order to verify that what we're about to do next will work. 

In this section were going to build an edge service. An edge service is the first port-of-call for requests destined for the downstream endpoints. Its placed in between the outside world and the many clients, and the downstream microservices. This central location makes it an ideal place in which to handle all sorts of requirements. 

An API does not a microservice make. Each microservice has non functional requirements that need to be addressed. Things like routing, comprssion, rate limiting, security, observabvility, etc need to be addressed for each microservice. Spring Boot can handle some of this in the Spring Boot application itself, but even trivial and minimally invasive concerns like rate limiting can become a maintenance burden at scale. An API gateway is a natural place in which to centrally address some of these concerns. 


Let's stand up a simple edge service that is part API adapter and part API gateway.

### The Build

We can generate a new project on the Spring Initializr. Specify an `Artifact ID`  (`gateway`, perhaps?) and then be sure to select `Wavefront`, `RSocket`, `Reactive Web`, `Gateway`, `Actuator`, `Lombok`, and specify `Java 11`. Click `Generate`. You'll now have a `.zip` file that you can unzip and import into your IDE. 

<!-- i wasnt sure about this part. normally id direct people to start.spring.io and have them incept the project there. but i guess in this case were giving them a prepopulated git directory? so maybe this next few paragraphs are more viable than the last one? -->

We'll need to make changes to the Apache Maven build. Here's what it should look like. 

// include: code/orders/pom.xml 

Note that this build includes dependencies for the `Wavefront` observability platform, `RSocket`, `Actuator`, `Lombok`,  `Reactive Web` and `Java 11`.

### The Java Code 

We'll need to make changes to the Java code. There are a few interesting players here. 

The application is a Spring Boot application, so well need the entry point class.

// include: code/gateway/src/main/java/com/example/gateway/GatewayApplication.java

The gateway will connect to the HTTP and RSocket endpoints and itll be conveient to manipulate the responses in terms of the same types that were used to create the responses. So, for the purposes of thie demonstration, well recreate the entities in the `customers` and `orders` modules as data transfer objects (DTOs) in this codebase.

Here's the `Order`: 

// include: code/gateway/src/main/java/com/example/gateway/Order.java

And here's the `Customer`: 

// include: code/gateway/src/main/java/com/example/gateway/Customer.java


Let's first look at building an API gateway. We'll use Spring Cloud Gateway to proxy one endpoint and forward requests onward to a downstream endpoint. SPring Cloud Gateways contract is simple: given a bean of type `RouteLocator`, Spring Cloud Gateway will create routes that match requests coming in from the outside, optionally process them in some way, and then forward those  requests onward. 

Yoyu can factory those `Route` instances in a number of different ways. Here, we're going to use the convenient `RouteLocatorBuilder` DSL. 

// include: code/gateway/src/main/java/com/example/gateway/ApiGatewayConfiguration.java

this class defines one route that matches any request headed to the host and port of the gateway (e.g,: `http://localhost:9999/`), having a path of `/c`, forwards the requests on to the downstream `customers` service (running on `localhost:8080`) 

And here's the `Customer`: 

// include: code/gateway/src/main/java/com/example/gateway/Customer.java

An edge service is also a natural place in which to introduce client translation logic or client specific views. In our example, were going to create a new HTTP endpoint, `/cos`, that returns the materialized view of the combined data from the RSocket `orders.{customerId}` endpoint and the HTTP `/customers` endpoint. We'll use reactive programming to make short of work of the scatter-gather service orchestration and composition. 

 
### The Configuration 

There are some things, like the port, and the logical name, that change from one service to another. We can spell those values out in properties in the application's `application.properties`. 

// include: code/orders/src/main/resource/application.properties 
// todo how do we add only two properties from that file, `server.port`, and `spring.application.name`. We'll add all the others later.

### Go Time 

Let's test it all out. Go to the root of the `orders` code and run: 

```shell
mvn clean spring-boot:run 
```

Use the [`rsc` CLI](https://github.com/making/rsc) to invoke the `orders.{customerId}` RSocket endpoint.

```shell
rsc tcp://localhost:8181 --stream  -r orders.3 
```





// todo show the API adapter code 

## Integrating the Spring Boot Actuator and Wavefront 

<!-- 
    make sure that they add the following properties to each microservice: 

    spring.application.name=customers
    management.endpoints.web.exposure.include=*
    management.endpoint.health.probes.enabled=true
    management.endpoint.health.show-details=always

    make sure that theyre aware that they have Wavefront installed. they can click the link on the console and see reflected in the console information automatically gathered about their application usage

    todo does the RSocket endpoint register metrics with Actuator like the HTTP endpoint does? i should check that out 
 -->

##  Buildpacks 

Weve got three microserives and we need to get them to the cloud. For most folks, and certainly anybody reading this eduk8s course, that means containers and Kubernetes. So, well need containerized versions of each of our applications. Don't freak out! I didn't say we're going to write `Dockerfile`s, I said that we need to get them into a container. _There's a difference_. 

Well use [buildpacks](https://buildpacks.io/) to transform your application source code into images that can run on any cloud. Buildpacks take an opinionated approach to containerizing applications. After all, how many different shapes could your Spring Boot, Django, Vue.js, .NET MVC, or Laravel projects have? How many different shapes does any app have, really? In java there are `.war` and `.jar` artifacts.  So, not that many, we'd reckon. A buildpack codifies the recipe for taking arbitrary applications of well-known shapes and turning them into a container. It analyzes the source code or source artifact that we give it and then creates a filesystem with sensible defaults that then gets containerized for us. A Spring Boot "fat" `.jar` will end up with a JDK, sensibly configured memory pools, etc. A client-side Vue.js application might land in an Nginx server on port 80. Whatever the result, you can then take that container and tag it in Docker and then push it to your container registry of choice. So, let's. 

// show the container registry for all three using GCR but keeping in mind that eduk8s has a custom container registry thing

## To the Cloud and Beyond!!

We've now got three applications deployed as containers. Let's get them runnig! We could craft a ton of YAML and then apply that, but there's not all that much  exciting about our containers, so we'll use a few `kubectl` shhortcuts to get a container up and running in production in no time. The only wrinkle is that our Spring Cloud Gateway application uses two properties to resolve teh hosts and ports of the `order` and `customers` services. Well configure a `ConfigMap` with the resolved hosts and ports of the services. Or will internal DNS work here? Let's find out.
