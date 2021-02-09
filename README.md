# The Cloud Native Java Workshop for SpringOne Tour 2021 

> I do not like work even when someone else is doing it.  ―Mark Twain

We're with Mr. Twain on this one. we loathe work, especially undifferentiated work - work that you have to do but that doesn't directly contribute to the success of your project, but getting to production today means doing more of it than ever. 

Cloud native computing refers not to any one single technology but more to an approach that optimizes for frequent and fast releases and speed of iteration. Microservices are a part of a broad continiium of technical approaches that, taken together, improve an organization's ability to release new software consistently and frequently. 

Microservices are all about optimizing for release velocity. The faster an organization's velocity the faster it can get working software into the hands of its users where it may delight them. the faster an organization;s velocity the faster it can learn from the experience and  improve in response. There are lots of work queues for each new release to production - things that must be done and that take wall clock take time. Some things - like integration and integration testing -  must be done in a serialized fashion, after all contributions have been made to a codebase, while other things may be done in parallel. The smaller the size of the codebase, the more quickly all the serialized work may be finished. The goal is to do as much work in parallel and to reduce the amount of serialized work, to reduce wall clock time between releases. Imrove these two things  - parallel and serial work -  and the wall time from concept to customer decreases significantly. It's no wonder microservices are popular! _Vroom vroom_.

But theyre not without cost. 

In the world of microservices, the rare tedium of addresssing non functional requirements like security, rate limiting, observability, routing, virtual machines and cloud infrastructure become an ongoing struggle that bedevil every new microservice. 

Microservices also introduce a lot of the complexity implied in building any distributed system. Things will fail in production. You'll need a toolkit that supports building responsive, efficient and scalable services. 

In this workshop, you're goign to run through a number of different technologies that let us have our cake and eat it too: you'll embrace microservices, and hopefully make a huge first step in the right direction towards production, and you'll do so in a way that pays down technical debt.

## A Service Chassis 
<!-- service chassis -->
Youre going to build three microservices: two APIs and one API gateway. The two APIs will be technically very similar to each other. One microservice, the `customers` application, manages `Customer` data. The other microservice, the `orders` application, manages `Order` data. 

NOTE: There's a strong case to be made that `order` data is part of the `customer` aggregate, but I always  like to imagine that something like an `order` would endure even if the `customer` were to somehow delete their records and orphan the `orders` because of revenue recognition and fiscal reporting requirements. It would make more sense to tombstone the `order`, wouldn't it? This thinking informs why we've teased this domain into two different microservices. 

Through consistency comes velocity. We'll use Spring Boot to code-generate a brand new Java based application with consistent defaults and eaasy-to-override features. Spring Boot is an opinionated approach to the Java ecosystem. You  opt-in to more automatic configuration ("auto-config") by adding so-called "starter" dependencies to the build. The mere presence of these starter dependencies introduces convenient, sensibly defaulted, behvior to an aplication. 

Chris Richardson coined the pattern _microservice chassis_, whihc basically describes an opionated, automatic framework like Spring Boot whose purpose is to reduce cross cutting concersn for each new application. 


## The Customer Service 

Let's stand up a simple service to handle `Customer` data.  We can generate a new project on the Spring INitializr. Specify an `Artifact` ID and then be sure to select `Reactive Web`, `Actuator`, `Lombok`, `H2`, and `Java 11`. Click `Generate`. You'll now have a `.zip` file that you can unzip and import into your IDE. 

We'll need to make changes to the Apache Maven build. Here's what it should look like. 

// include: code/customers/pom.xml 


Well need to make changes to the Java code. Here's what it should look  like.

// include: code/customers/src/main/java/com/example/customers/CustomersApplication.java

## Reactive Programming 

This application uses reactive programming, which you may note from the `Flux<T>`, `Mono<T>` and `Publisher<T>` types strewn about the codebase. These types are a sort of inverted `Collection<T>` where, instead of pulling data out of the collection to _pull_ the data out of it when you want it, the data is _pushed_ to you when the data is ready. This inverted approach means that Spring Webflux, the reactive web framweokr that we're using, does not need to wait for an asynchronous value of stream of values to resolve. It can ask for it and then carry on doing other work until the results arrive. This means that no thread is ever parked - _idle_ -  waiting for data to arrive, which means the runtime can repurpose those threads to acheive much better scalability. In the cloud, scalability means less hardware being used to do the same job, which translates into reduced data center spend. Scalability is a _good thing_ (TM). 

Reactive programming also gives us two other major benefits: ease of composition and robustness. You see, reactive programming forces us to think of everything in terms of the Reacttive Streams `Publisher<T>` type or its specializations `Flux<T>` (a container type for `0..N` values of type `T`) and `Mono<T>` (a container type for at-most one value). All reactive Spring APIs use these types. 

The code showcases a number of interetin elemetns. `Customer` is an entity object that maps to data read from the in-memory SQL database, H2. The `CustomerRepository` is a Spring Data repository that allows us to read and write entity data. `CustomersListener` installs some sample data into the database when the application starts up. `CustomerRestController` describes an HTTP endpoint, `/customers`. 


* api gateways 
* reactive orders, customers, gateway
* gateway lets uss recentalize some crss cutting concerns so that we can with a miniumum of fuss address some of the redudnant concerns for each new microservice 
* reactive api code and persistence 
* Actuator & /actuator/metrics 
* orders, customers, gateway will all use the Wavefront starter (Cora?)
* use graalvm native images 
* use spring-boot:build-image to build a docker image 
* use docker to tag the image and push it to a docker registry 
* use kubernetes to then create a deployment with the images and see it scaled