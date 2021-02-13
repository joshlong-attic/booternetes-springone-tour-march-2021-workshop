# The Cloud Native Java Workshop for SpringOne Tour 2021 

> I do not like work even when someone else is doing it. ― Mark Twain

We're with Mr. Twain on this one. We loathe work, especially undifferentiated work. Work that you have to do, but that doesn't directly contribute to your project's success is undifferentiated work. Getting to production today, it seems, means doing more of it than ever. 

When we talk about _cloud native computing_, it refers not to any single technology but more to an approach that optimizes for frequent and fast releases and the speed of iteration. Faster organizations learn and grow faster. There are many work queues for each new production release, which must be done and take wall-clock time. After all the contributions to the codebase settle, some things like integration and integration testing must happen serially. At the same time, it's possible to do other work concurrently. The smaller the size of the codebase, the more quickly all the serialized work finishes. The goal is to do as much work in parallel and reduce the amount of serialized work, to reduce wall clock time between releases. With their smaller codebases and smaller teams, microservices reduce the wall clock time between having an idea and seeing it deployed into production. 

Microservices are not without costs. 

In the world of microservices, the rare work (tedium!) of addressing non-functional requirements like security, rate limiting, observability, routing, containers, virtual machines, and cloud infrastructure has become an ongoing struggle that bedevils every new microservice. 

Microservices introduce a lot of the complexity implied in building any distributed system. Things will fail in production at a sufficient scale. In this workshop, you're going to look at different technologies that let us pay down some technical complexity and technical debt of scale.

## Spring Boot: the Service Chassis 

You're going to build three microservices: two APIs and one API gateway. The two APIs will be technically very similar to each other. One microservice, the `customers` application, manages `Customer` data. The other microservice, the `orders` application, manages `Order` data. Finally, the `gateway` acts as a proxy and an edge service and addresses cross-cutting concerns.

NOTE: There's a strong case that `order` data is part of the `customer` aggregate. I always like to imagine something like an `order` would endure even if the `customer` were to delete their records and orphan somehow the `orders` for revenue recognition purposes and fiscal reporting requirements. It would make more sense to tombstone the `order`, wouldn't it? This thinking informs why we've teased this domain into two different microservices. 

Through consistency comes velocity. Spring Boot is an opinionated approach to the Java ecosystem that provides consistent defaults and easy-to-override features. We'll use Spring Boot to build a new Java application. You opt-in to a default configuration ("auto-config") for a particular part (e.g., serving HTTP endpoints, data access, security) by adding so-called "starter" dependencies to the build. The mere presence of these starter dependencies on the classpath activates default features in the application.

Chris Richardson coined the pattern _microservice chassis_ to describe an opinionated, automatic framework like Spring Boot that reduces concerns for each new application. 

## The Customer Service 

Let's stand up a simple service to handle `Customer` data. 

### The Build

We will change to the Apache Maven build. 

// include: code/orders/pom.xml 

This build includes dependencies for `R2DBC`, the `Wavefront` observability platform, `Reactive Web`, `Actuator`, `Lombok`, the `H2`, and `Java 11`.

### The Java Code 

Let's look at the Java code. There are a few exciting players here. We'll need an entry point class.

// include: code/orders/src/main/java/com/example/orders/OrdersApplication.java

We're going to read and write data to a SQL database table called `orders`. We'll need an entity class.

// include: code/orders/src/main/java/com/example/orders/Order.java

We'll need a Spring Data repository.

// include: code/orders/src/main/java/com/example/orders/OrderRepository.java

We're going to read and write data to a SQL database table called `orders`. The H2 SQL database is an embedded, in-memory SQL database that will lose its state on every restart. We'll need to initialize it. 

// include: code/customers/src/main/java/com/example/customers/CustomersListener.java

And, finally, we want to export an HTTP endpoint, `/customers`. 

// include: code/customers/src/main/java/com/example/customers/CustomerRestController.java

### The Configuration 

Like the port and the logical name, some things change from one service to another. We will specify these values in the application's `application.properties`. 

// include: code/customers/src/main/resources/application.properties 

<!-- todo how do we add only two properties from that file, `server.port`, and `spring.application.name`. We'll add all the others later. -->

### Go Time 

Let's test it all out. Go to the root of the `customers` code and run: 

"`shell
mvn -f pom.xml clean spring-boot:run 
```

Use the `curl` CLI to invoke the `/customers` HTTP endpoint and confirm that you're given some data in response. 

"`shell
curl http://localhost:8080/customers
```


The code showcases several interesting elements. `Customer` is an entity object that maps to data read from the in-memory SQL database, H2. The `CustomerRepository` is a Spring Data repository that allows us to read and write entity data. `CustomersListener` installs some sample data into the database when the application starts up. `CustomerRestController` describes an HTTP endpoint, `/customers`. 



## Reactive Programming 

This application uses [reactive programming](https://spring.io/reactive), which you may recognize from the telltale `Flux<T>`, `Mono<T>` and `Publisher<T>` types strewn about the codebase. Reactive programming requires the [the Reactive Streams](http://www.reactive-streams.org) specification. From the website: "Reactive Streams project is an initiative to provide a standard for asynchronous stream processing with non-blocking backpressure."

The [Reactor Project](https://projectreactor.io/) builds upon the Reactive Streams specification. It provides two specializations, `Flux<T>` (a container type for `0..N` values of type `T`) and `Mono<T>` (a container type for at-most one value). Types from `org.reactivestreams.*` are the Reactive Streams specification. In the Reactive Streams specification, a reactive stream `Publisher<T>` publishes data asynchronously. A `Subscriber<T>` consumes data from a `Publisher<T>`. A `Subscriber<T>` may request more data, or cancel the data altogether, using the `Subscriber<T>`. The `Subscriber<T>` regulates the flow of data; it supports flow control. Flow control is sometimes interchangeably referred to as _backpressure_.

The Reactive Streams `Publisher<T>` are a sort of inverted `Collection<T>`. Instead of pulling data out of the collection when you want it, the data is _pushed_ to you when the data is ready. The inverted approach supports better **scalability**; it means that [Spring Webflux](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html), the reactive web framework that we're using, does not need to wait for an asynchronous value, or a stream of values, to resolve. The runtime can carry on doing other work while results arrive. No thread is ever parked - _idle_ - waiting for data to arrive, which means the runtime can repurpose those threads to handle other work, resulting in better scalability. In the cloud, scalability means less hardware being used to do the same job, translating into reduced data center spend. Scalability is a _Good Thing_. 

<!-- Reactive Programming gives ease of composition. and robustness -->

Reactive programming simplifies the work of **composition and integration**. All reactive Spring APIs use these Reactive Streams and Reactor types. Various projects (Spring Webflux, Spring Integration, Spring Security, Spring Data, Spring Cloud, Spring Boot, Spring Framework, and others we've surely forgotten) all have reactive programming support.  You can interoperate with other projects like RxJava, Akka Streams, Vert.x, or any other library that produces and consumes `Publisher<T>'s. It is possible to adapt `CompletableFuture<T>`, `Thread`, `Collection<T>`, arrays, `Iterable<T>`, and other types to Reactive Streams types, simplifying their composition. 

Things fail on the network. Statistically, the larger your system's surface area, the more likely some part of it will eventually fail. Reactive programming acknowledges that reality and strives to improve the *robustness* of our code, providing operators to support retries, timeouts, error handling, and more.

## RSocket 

While we love HTTP, broadly, and the REST constraint on HTTP, specifically, as much as the next cloud-native, it's not the only game in town when it comes to high speed, low-latency, highly scalable, interservice communication. There are many alternatives like GraphQL, GRPC, and - our perennial favorite - RSocket. RSocket is a binary protocol that reifies the concepts of the Reactive Streams in the wire protocol. RSocket understands supports critical components of reactive programming, including _backpressure_, and has ways to communicate that information on the wire itself. The protocol is, of course, platform, language, and payload agnostic. It follows that there is a fantastic Java client written on top of Project Reactor that we could use independent of Spring if we were so inclined. But, as luck would have it, Spring already integrates RSocket and provides a component model that makes trivial the work of standing up an RSocket based service. 


## The Orders Service 


Let's stand up a simple service to handle `Order` data. 



### The Build

We'll need to make changes to the Apache Maven build. 

// include: code/orders/pom.xml 

This build includes dependencies for `R2DBC`, the `Wavefront` observability platform, `RSocket`, `Actuator`, `Lombok`, `H2`, and `Java 11`. 

### The Java Code 

Let's look at the Java code. There are a few exciting players here. We'll need an entry point class.

// include: code/orders/src/main/java/com/example/orders/OrdersApplication.java

We're going to read and write data to a SQL database table called `orders`. We'll need an entity class.

// include: code/orders/src/main/java/com/example/orders/Order.java

And we'll need a Spring Data repository.

// include: code/orders/src/main/java/com/example/orders/OrderRepository.java

We're going to read and write data to a SQL database table called `orders`. The H2 SQL database is an embedded, in-memory SQL database that will lose all state on every restart. We'll need to initialize it. 

// include: code/orders/src/main/java/com/example/orders/OrdersListener.java

And, finally, we want to export an HTTP endpoint, `/orders`. 

// include: code/orders/src/main/java/com/example/orders/OrderRSocketController.java 

### The Configuration 

Like the port and the logical name, some things change from one service to another. We can spell those values out in properties in the application's `application.properties`. 

// include: code/orders/src/main/resources/application.properties 
// todo how do we add only two properties from that file, `server.port`, and `spring.application.name`. We'll add all the others later.



### Go Time 

Let's test it all out. Go to the root of the `orders` code and run: 

"`shell
mvn clean spring-boot:run 
```

Use the [`rsc` CLI](https://github.com/making/rsc) to invoke the `orders.{customerId}` RSocket endpoint.

```shell
rsc tcp://localhost:8181 --stream -r orders.3 
```

## Living on the Edge 

We're going to need both microservices running to verify that what we're about to build next will work. 

In this section, we're going to build an edge service. An edge service is the first port-of-call, the first stop en route to the final destination, for requests destined for the downstream endpoints. An edge service sits between the outside world, and its many clients, and the downstream microservices. This central location makes it an ideal place in which to handle all sorts of requirements. 

Each microservice must address non-functional, cross-cutting concerns like routing, compression, rate limiting, security, and observability. Spring Boot can handle some of this. Still, even trivial and minimally invasive concerns like rate limiting can become a maintenance burden at scale. An API gateway is a natural place in which to address some of these concerns centrally. 

Let's stand up a simple edge service that is part API adapter and part API gateway.

### The Build

We'll need to make changes to the Apache Maven build. 

// include: code/orders/pom.xml 

This build includes dependencies for the `Wavefront` observability platform, `RSocket`, `Actuator`, `Lombok`, `Reactive Web`, and `Java 11`.

### The Java Code 

Let's look at the Java code. There are a few exciting players here. We'll need an entry point class.

// include: code/gateway/src/main/java/com/example/gateway/GatewayApplication.java

The gateway will connect to the HTTP and RSocket APIs. It'll be convenient to manipulate the responses in terms of the same types used to create the responses. We'll recreate the entities in the `customers` and `orders` modules as data transfer objects (DTOs) in this codebase.

Here's the `Order`: 

// include: code/gateway/src/main/java/com/example/gateway/Order.java

And here's the `Customer`: 

// include: code/gateway/src/main/java/com/example/gateway/Customer.java

We're going to provide a new view of the data, so we will need a DTO called `CustomerOrders`. 

// include: code/gateway/src/main/java/com/example/gateway/CustomerOrders.java

Let's first look at building an API gateway. We'll use Spring Cloud Gateway to proxy one endpoint and forward requests onward to a downstream endpoint. Spring Cloud Gateway's contract is simple: given a bean of type `RouteLocator`, Spring Cloud Gateway will match the described routes in incoming requests, optionally process them in some way with filters, and then forward those requests onward. 

You can factory those `Route` instances in several different ways. Here, we're going to use the convenient `RouteLocatorBuilder` DSL. 

// include: code/gateway/src/main/java/com/example/gateway/ApiGatewayConfiguration.java

Our gateway defines one route that matches any request headed to the gateway's host and port (e.g.,: `http://localhost:9999/`) with a  path (`/c`). Spring Cloud Gateway will forward the requests to the downstream `customers` service (running on `localhost:8585`). Filters sit in the middle of this exchange and change the request as it goes to the downstream service or the response as it returns from the downstream service. 

An edge-service is also a natural place to introduce client translation logic or client-specific views. Clients often need specific views of data. They may require more awareness of the payloads going to and from a particular endpoint. We will create a new HTTP endpoint (`/cos`) in the `gateway` module that returns the materialized view of the combined data from the RSocket `orders` endpoint and the HTTP `orders` endpoint. We'll use reactive programming to handle the scatter-gather service orchestration and composition. The client need never know that the response contains data from two distinct data sources.

For HTTP interactions, we will configure an instance of the reactive, non-blocking `WebClient`. For RSocket interactions, we will configure an example of the reactive, non-blocking `RSocketRequester`. Note that because communication between RSocket nodes is bidirectional, it's not appropriate to think of one side as the _client_ and the other the _service_. Either side of an RSocket connection may act as either a client or a service. So, the framework provides the `RSocketRequester`, not the `RSocketClient`. 

// include: code/gateway/src/main/java/com/example/gateway/ApiAdapterConfiguration.java

We use these two types - `RSocketRequester` and `WebClient` - to create a client to our various microservices, `CrmClient`. 

// include: code/gateway/src/main/java/com/example/gateway/CrmClient.java

`CrmClient` offers three public methods. The first, `getCustomers`, calls the HTTP service and returns all the `Customer` records. The second method, `getOrdersFor`, returns all the `Order` records for a given `Customer`. The third method, `getCustomerOrders`, mixes both of these methods and provides a composite view. The fourth method, `applySlaDefaults`, uses `Flux<T>`'s operators to apply some useful defaults to all of our reactive streams. The stream will degrade gracefully if a request fails (`onErrorResume`). It will use a timeout (`timeout`) to abandon the request after a time interval has elapsed. The stream will retry (`retryWhen`) the request with a growing backoff period between subsequent attempts. 

<!-- We'll need to stand up an HTTP endpoint that people can use to get the materialized view.  -->
Let's standup the HTTP endpoint for our materialized data.

// include: code/gateway/src/main/java/com/example/gateway/CustomerOrdersRestController.java

### The Configuration 

The port and the logical name for each of the services vary. There are default values for the other services' host and ports (`gateway.orders.hostname-and-port` and `gateway.customers.hostname-and-port`) specified in `application.properties` that work on `localhost`, but that won't work in production. We'll need to override them when it comes time to deploy. Spring Boot supports [12-factor style configuration](https://12factor.net/config), so we will redefine default values in production (without recompiling the application binaries). We'll use a Kubernetes `ConfigMap` to define values exposed to the application as environment variables, overriding the default values defined in `application.properties`.

// include: code/orders/src/main/resources/application.properties 

<!-- todo how do we add only two properties from that file, `server.port`, and `spring.application.name`. The gateway also has two specific, custom properties, `gateway.orders.hostname-and-port` and `gateway.customers.hostname-and-port`. We'll add all the others later. -->

We want some values only to be active when a particular condition is met. We were going to use Spring's concept of a profile, a label that - once switched on - could result in some specific configuration being executed or activated. You can use labels to parameterize the application's runtime environment and execution in different environments (e.g.: `production`, `staging`, `dev`). We will run the `gateway` application with the `SPRING_PROFLES_ACTIVE` environment variable set to `cloud`. Our Spring-based gateway application will start up, see an environment variable indicating that a particular profile should be active, and then load the regular configuration _and_ the profile-specific configuration into one hierarchy of configuration values. In this case, the profile-specific configuration lives in `application-cloud.properties`. 

// include: code/gateway/src/main/resources/application-cloud.properties 


### Go Time 

Let's test it all out. Go to the root of the `gateway` code and run: 

"`shell
mvn clean spring-boot:run 
```

Use the `curl` CLI to invoke the `/cos` HTTP endpoint.

"`shell
curl http://localhost:9999/cos 
```

You should be staring at JSON containing both your customer data and the orders for each customer. Congratulations! 


## Integrating Observability with the Spring Boot Actuator Module and Wavefront 

<!-- 
make sure that they add the following properties to each microservice: 

spring.application.name=customers
management.endpoints.web.exposure.include=*
management.endpoint.health.probes.enabled=true
management.endpoint.health.show-details=always

Make sure that they're aware that they have Wavefront installed. they can click the link on the console and see reflected in the console information automatically gathered about their application usage

Todo, does the RSocket endpoint register metrics with Actuator as the HTTP endpoint does? I should check that out 
-->

We've sort of eschewed the question of observability. Observability is the idea that we can understand the state of a system by observing its outputs.   

For low cardinality data -  e.g., the number of requests to a given URL, the number of orders made, and the number of customers signed up - metrics are a natural fit. Metrics are just numbers mapped to a logical name. They're statistics like the total, the average, the median value, the 95th percentile. You can use   [Micrometer](http://micrometer.io) to capture this kind of data. Spring Boot's Actuator module integrates Micrometer to capture all sorts of useful metrics out of the box. You can use Micrometer to capture your metrics - key performance indicators (KPIs) - directly. 

The Spring Boot Actuator is a set of managed HTTP endpoints that expose useful information about an application, like metrics, health checks, and the current environment. After all, whom better to articulate the state of a given service than the service itself?

Once added to the `application.properties` files of all three modules, the following properties expand the data exposed from the Actuator endpoints. Once you've applied the configuration,   inspect the following HTTP endpoints: 

 * `/actuator` to see all the available endpoints 
 * `/actuator/metrics` for the metrics information 
 * `/actuator/health` for the health endpoint, and the Kubernetes liveness and readiness probes
 * `/actuator/env` for the application's environment variables.

These are only a sampling of the endpoints and opportunities that Spring Boot Actuator represents. 

<!-- something the user can click to add these properties to the `application.properites` fiels of all three modules:

management.endpoints.web.exposure.include=*
management.endpoint.health.probes.enabled=true
management.endpoint.health.show-details=always
management.health.probes.enabled=true


TODO we need to describe how to use the  just-configured probes endpoints in the Kubernetes YAML later on

-->

For high cardinality data, like individualized requests, distributed tracing is a good fit. Distributed traces are just a log of the path a request has taken from one node to another. You can use Spring Cloud Sleuth to capture this kind of data. 

Both Micrometer and Spring Cloud Sleuth are abstractions that work with numerous, diverse backends. You might use Micrometer to talk to services like Wavefront, DataDog, Prometheus, Graphana. Equally, you may use Spring Cloud Sleuth to talk to services like Wavefront, OpenZipkin, and Google Cloud Stack Driver Trace. We happen to like [Tanzu Wavefront](https://tanzu.vmware.com/observability) because it supports both kinds of data and makes it trivial to cross-reference the distinct types of data. 

The Spring Boot starter for Wavefront is on the classpath of all three modules, and it's _already_ working! Wavefront is the _easiest_ part of this whole workshop! Look at the logs of one of your applications running on your local machine, and you'll see a URL and some Spring Boot properties. Copy and paste the URL and paste it into a browser. Some shells even support clicking the link inside the shell. You'll end up staring at a freemium Wavefront account complete with a `Spring Boot Dashboard` dashboard highlighting data that would be useful from a Spring Boot application's perspective. Drive a few requests (`/cos`, `/proxy`, etc.) in the application and then wait a few minutes. The first few requests take a few minutes to percolate and arrive in the system. 

The log output will contain some Spring Boot properties containing a token. Copy those configuration values and add them to the property files for all of the other applications. Restart the applications, and you'll see the data in the dashboard.

<!-- could we have them add those properties dumped in the console to all three application.properties? then redeploy -->

You might check out   Tanzu Observability Engineering leader Sushant Dewan, Sr. Product Marketing Manager Gordana Neskovic, and Josh Long's webinar on [Wavefront and Spring Boot](https://www.brighttalk.com/webcast/14893/413305/tanzu-observability-tips-for-understanding-your-spring-boot-applications). 

You might like this blog  - [_Metrics and Tracing: Better Together_](https://spring.io/blog/2021/02/09/metrics-and-tracing-better-together)  - by Tanzu Observability Engineer Tommy Ludwig and Josh Long.

## Buildpacks 

We've got three microservices, and we need to get them to the cloud. For anybody reading this Eduk8s course, the _cloud_ means containers, and it means Kubernetes. We'll need containerized versions of each of our applications. Don't freak out! I didn't say we're going to write a `Dockerfile`; I said we need to put our applications into containers. _ There's a difference_. 

We'll use [buildpacks](https://buildpacks.io/) to transform application source code into images that can run on any cloud. Buildpacks take an opinionated approach to containerizing applications. How many different shapes could your Spring Boot, Django, Vue.js, .NET MVC, or Laravel projects have? How many different shapes does _any_ application have? In  Java, application binaries commonly come in two shapes:  there are `.war` artifacts (deployed in a Servlet container) and so-called "fat" `.jar` artifacts (run with `java`). So, not that many, we'd reckon. A buildpack codifies the recipe for taking arbitrary applications of well-known shapes from different languages and runtimes and turning them into containers. A buildpack will analyze the source code or source artifact that we give it and then create a filesystem with sensible defaults that the buildpack will containerize. A Spring Boot "fat" `.jar`   ends up with a Java runtime and sensibly configured memory pools. A client-side Vue.js application might land in an Nginx server serving static assets.  Whatever the result, you can then take that container and tag it in Docker, and then push it to your container registry of choice. So, let's. 

<!--  

 todo show the container registry for all three using GCR but keeping in mind that eduk8s has a custom container registry thing 

 I am hoping for some help writing this part. I would show something like the `build_container` function in `deploy.sh` at this point. I'm a little hazy on how the Eduk8s thing works otherwise
-->

## To the Cloud and Beyond 

Let's look at deploying our newly minted containers and running them in Kubernetes. We could craft a ton of YAML and then apply that. We'll write some YAML to get a container up and running in production in no time. The only wrinkle is that our applications will need to change specific values based on environment variables in the container.

We've written a shell script that executes everything required to deploy this application to production. 

// include: code/deploy/deploy.sh 

In turn, the script applies three different Kubernetes configuration files, `customers.yaml`, `orders.yaml`, and `gateway.yaml`. 

Here are those files. First, we'll look at the `orders` service.

// include: code/deploy/orders.yaml

And then the `customers` service. 

// include: code/deploy/customers.yaml

and then finally, the `gateway` code 

// include: code/deploy/customers.yaml

You need to execute `deploy.sh` to get everything installed into a Kubernetes instance. 

<!-- 
At this point, I would show the `deploy_to_kubernetes` function in deploy.sh get the binaries deployed to production
 -->

The script assumes the availability of a namespace called `booternetes`; it will create the namespace if it doesn't exist. You should be able to run the following incantation to see all the newly created resources. 

"`shell
kubectl get all -n booternetes 
```

You can see what is in your Kubernetes cluster. We're interested in the IP address of the `gateway` code. Inspect the `EXTERNAL_IP` value present for the `gateway` module in the output. Enter that into your browser followed by `/cos`, and you should get the results from both the `orders` and `customers` service.

## Summary 

Isn't that satisfying? We got something up and running in 90 minutes. We've only just begun to scratch the surface of all the possibilities. In this cloud native workshop, we've looked at a lot of different features in Spring Boot to support building cloud native software:

* reactive HTTP 
* reactive RSocket 
* reactive data access with R2DBC  
* observability with Spring Boot's Actuator and the Tanzu Wavefront observability platform
* features in Spring Boot designed to support production environments 
* buildpacks, which enable easy convention-over-configuration centric containerization
* how to deploy our containers to a container registry 
* how to deploy those containers to a Kubernetes cluster


## Next Steps

We've tried to link to some content you might find useful where, on occasion, we felt it reasonable to do so. You should remember to clean up the resources you're using in your Kubernetes cluster. 


```shell 
kubectl delete ns/booternetes 
````

Your production journey should be much easier for the sane defaults that things like Spring Boot, buildpacks, and Kubernetes itself can provide us.
