# The Cloud Native Java Workshop for SpringOne Tour 2021 

> I do not like work even when someone else is doing it.  ―Mark Twain

We're with Mr. Twain on this one. we loathe work, especially undifferentiated work - work that you have to do but that doesn't directly contribute to the success of your project, but getting to production today means doing more of it than ever. 

Cloud native computing refers not to any one single technology but more to an approach that optimizes for frequent and fast releases and speed of iteration. Microservices are a part of a broad continiium of technical approaches that, taken together, improve an organization's ability to release new software consistently and frequently. 

Microservices are all about optimizing for release velocity. The faster an organization's velocity the faster it can get working software into the hands of its users where it may delight them. the faster an organization;s velocity the faster it can learn from the experience and  improve in response. There are lots of work queues for each new release to production - things that must be done and that take wall clock take time. Some things - like integration and integration testing -  must be done in a serialized fashion, after all contributions have been made to a codebase, while other things may be done in parallel. The smaller the size of the codebase, the more quickly all the serialized work may be finished. The goal is to do as much work in parallel and to reduce the amount of serialized work, to reduce wall clock time between releases. Imrove these two things  - parallel and serial work -  and the wall time from concept to customer decreases significantly. It's no wonder microservices are popular! _Vroom vroom_.

But theyre not without cost. 

In the world of microservices, the rare tedium of addresssing non functional requirements like security, rate limiting, observability, routing, virtual machines and cloud infrastructure become an ongoing struggle that bedevil every new microservice. 

Microservices also introduce a lot of complexity that is implied in building any distributed system. 

* reactive orders, customers, gateway
* reactive api code and persistence 
* Actuator & /actuator/metrics 
* orders, customers, gateway will all use the Wavefront starter (Cora?)
* use graalvm native images 
* use spring-boot:build-image to build a docker image 
* use docker to tag the image and push it to a docker registry 
* use kubernetes to then create a deployment with the images and see it scaled