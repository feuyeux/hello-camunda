# Events, Flows and Long-Running Services: A Modern Approach to Workflow Automation

https://www.infoq.com/articles/events-workflow-automation DEC 21, 2017

### Key Takeaways

- Recent discussions around the microservice architectural style has promoted the idea of event-driven-architectures to effectively decouple your services.
- Domain events are great for decentralised data-management, generating read-models or tackling cross-cutting concerns. However, you should not implement complex peer-to-peer event chains. Using commands to coordinate other services will reduce your coupling even further.
- Centrally managed ESBs don’t fit into a microservices architecture. Smart endpoints and dumb pipes are preferable. However, don’t dismiss coordinating services for fear of introducing central control: important business capabilities need a home.
- In the past, BPM and workflow engines were very vendor-driven, and so there are many horrible “zero-code” tools in the market scaring away developers. However, lightweight and easy-to-use frameworks now exist, and many of them are open source. 
- Do not invest time in writing your own state machines, but instead leverage existing workflow tools. This will help you to avoid accidental complexity.

 

If you have been following the recent discussions around the microservice architectural style, you may have heard this advice: “To effectively decouple your (micro)services you have to create an event-driven-architecture”.

The idea is backed by the [Domain-Driven Design (DDD)](https://www.infoq.com/minibooks/domain-driven-design-quickly) community, by providing the nuts and bolts for leveraging domain events and by showing how they change the way we think about systems.

Although we are generally supportive of event orientation, we asked ourselves what risks arise if we use them without further reflection. To answer this question we reviewed three common hypotheses:

- Events decrease coupling
- Central control needs to be avoided
- Workflow engines are painful (this might seem unrelated, but we will show the relevant connection later on)

We have previously presented the results of our review within a talk that has been delivered at [muCon London](https://skillsmatter.com/conferences/8549-con-2017-the-microservices-conference) 2017 ([slides](https://medium.com/r/?url=https%3A%2F%2Fwww.slideshare.net%2FBerndRuecker%2Fmucon-london-2017-break-your-event-chains) and [recording ](https://medium.com/r/?url=https%3A%2F%2Fskillsmatter.com%2Fskillscasts%2F10718-break-your-event-chains)available), [O’Reilly Software Architecture London](https://conferences.oreilly.com/software-architecture/sa-eu) ([slides](https://medium.com/r/?url=https%3A%2F%2Fwww.slideshare.net%2FBerndRuecker%2Foreilly-sa-complex-event-flows-in-distributed-systems) and [recording](https://medium.com/r/?url=https%3A%2F%2Fwww.safaribooksonline.com%2Flibrary%2Fview%2Foreilly-software-architecture%2F9781491985274%2Fvideo315427.html)) and [KanDDDinsky](https://kandddinsky.de/) ([slides ](https://medium.com/r/?url=https%3A%2F%2Fwww.slideshare.net%2FBerndRuecker%2Fkandddinsky-let-your-domain-events-flow)and [recording](https://medium.com/r/?url=https%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3DLKoaMbqZp9Y)).

The talk is based on experiences in different real-life projects, but uses a fictional retail example motivated by the order fulfillment process of Zalando (as shared in [this meetup](https://medium.com/r/?url=https%3A%2F%2Fwww.meetup.com%2FZalando-Tech-Events-Berlin%2Fevents%2F242890035%2F)).

We assume we have four [bounded contexts](https://www.infoq.com/articles/ddd-contextmapping) resulting in four dedicated services (which might be [microservices](https://martinfowler.com/articles/microservices.html), [self-contained systems](https://www.infoq.com/articles/scs-microservices-done-right) or some other form of [service](https://www.infoq.com/articles/service-oriented-architecture-and-legacy-systems)):

![img](https://imgopt.infoq.com/fit-in/1200x2400/filters:quality(80)/filters:no_upscale()/articles/events-workflow-management/en/resources/3fig1-1513668200412.png)

## How to decouple using events

Let’s assume the Checkout service should give feedback to the user if an item is in stock and can be shipped right away. The Checkout service can ask the Inventory service about the amount in stock using request/response, but this couples Checkout to Inventory (in terms of availability, response times etc).

An alternative approach is that Inventory publishes any change to the amount of in-stock items as a broadcasted event to let the world know about it. Checkout can listen to these events and save the current amount in stock internally, and then answer related questions locally. The information is obviously a copy, and might not be absolutely consistent. However, some degree of eventual consistency is typically sufficient and a necessary tradeoff in distributed systems.

![img](https://imgopt.infoq.com/fit-in/1200x2400/filters:quality(80)/filters:no_upscale()/articles/events-workflow-management/en/resources/5fig3-1513669724066.png)

Another use case is to hook in non-core functionality like cross-cutting concerns. Consider that you want to send out customer notifications for important steps in your order fulfillment. A Notification service could be implemented in a completely autonomous manner and store notification preferences and contact data for customers. It can then send customer emails on certain events like “Payment Received” or “Goods Shipped” without any change required in other services. This makes [event-driven architectures (EDA)](https://martinfowler.com/articles/201701-event-driven.html) very flexible and it can become simple to add new services or extend existing ones.

## The risk of peer-to-peer event chains

Once teams get started with event-driven-architectures, they often become obsessed with events - events provide amazing decoupling , so let’s use them for everything! Problems start to arise when you implement end-to-end flows like the order fulfillment business process via peer-to-peer event chains. Let’s assume a rather trivial flow. It could be implemented in a way that the next service in the chain always knows when it has to do something

![img](https://imgopt.infoq.com/fit-in/1200x2400/filters:quality(80)/filters:no_upscale()/articles/events-workflow-management/en/resources/6fig3-1513669723708.png)

This works. However, the problem is that no one ever has a clear overview, making the flow hard to understand and - more importantly - hard to change. Additionally, keep in mind that realistic flows are not that simple, and typically involve many more services. We have seen microservice landscapes where this leads to a situation where a complex system of services does something, but nobody really knows what exactly, or how.

Now, think of how we would implement a simple change request to fetch the goods before we do the payment:

![img](https://imgopt.infoq.com/fit-in/1200x2400/filters:quality(80)/filters:no_upscale()/articles/events-workflow-management/en/resources/3fig4-1513669723931.png)

You have to adjust and redeploy several services for a simple change in the sequence of steps. This is generally an anti pattern within the context of microservices, as the core design principle of this architectural style is to strive for less coupling and more service autonomy. Accordingly, we advise you to think twice before using events to implement peer-to-peer flows, especially when expecting considerable complexity.

## Commands, but without the need for central control

A more sensible approach to tackle this flow is to implement it in a dedicated service. This service can act as a coordinator, and send commands to the others -- for example, to initiate the payment. This is often a more natural approach, as in this case we would generally not consider this a good design if the Payment service had knowledge about all of its consumers by subscribing to manyfold business events triggering payment retrieval. The following scenario avoids this coupling, and could be described as an orchestrated approach: “Order orchestrates Payment, Inventory and Shipment to fulfill the business for the customer.”

![img](https://imgopt.infoq.com/fit-in/1200x2400/filters:quality(80)/filters:no_upscale()/articles/events-workflow-management/en/resources/3fig5-1513673353429.png)

However, as soon as we speak about orchestration, some people think of “magical” [Enterprise Service Buses (ESBs)](https://www.infoq.com/presentations/Enterprise-Service-Bus) or centralised [Business Process Modelling (BPM)](https://www.infoq.com/articles/seven-fallacies-of-bpm) solutions. There were a lot of bad experiences with related tools in the past, as this often meant you had to give up easy testability or deployment automation for complex, proprietary tooling. James Lewis and Martin Fowler laid down some of the foundations for microservices architectures when he suggested to better use “[smart endpoints and dumb pipes](https://martinfowler.com/articles/microservices.html)”.

However, the picture above doesn’t suggest a smart pipe. The orchestrating service handles the order fulfillment as a first-class citizen and implements it in a dedicated service: Order. Such services can be implemented in any way you like ,  with any technology stack you like. It is simply now that you have a dedicated place where you can understand the flow, and change it by changing one service only.

Sam Newman describes another risk of such an orchestrating Order service in his book [Building Microservices](http://samnewman.io/books/building_microservices/) -- over time this can develop into a “god service“ sucking in all business logic while others degrade into “anemic” services, or in an even worse case become [CRUD-like “Entity” services](http://www.michaelnygard.com/blog/2017/12/the-entity-service-antipattern/). Does this happen because we sometimes prefer commands over events? Or is it because of orchestration? No. Let’s quickly revise Martin Fowler’s “smart endpoints”. What constitutes a smart endpoint? It’s about good API design. For the Payment service you could design an effective coarse-grained API that can act on the Retrieve Payment command and emit either a Payment Received or Payment Failed event. No internals of payment handling like customer credits or credit card failures are exposed. In this case, the service doesn’t get anemic just because it’s orchestrated (or to put it more simply, “used“) in some other context.

![img](https://imgopt.infoq.com/fit-in/1200x2400/filters:quality(80)/filters:no_upscale()/articles/events-workflow-management/en/resources/3fig6-1513673352616.png)

## Respect the potentially long running nature of services

In order to design smart endpoints and provide a valuable API to your clients, you must recognise that many services are potentially long running because they need to resolve business problems behind the scenes. Let’s assume that in case of an expired credit card we give the customer a chance to update it (taking inspiration from GitHub, where you have two weeks before they close your account after a payment failure). If the Payment service does not care about the details of waiting for the customer, it would push the responsibility for that requirement to its user , the Order service. However, it is much cleaner and more in line with the DDD idea of bounded contexts to keep that responsibility inside Payment. Waiting for a customer to provide new credit card details means that the payment could still either be retrieved, or fail. Thus, the Payment API becomes very clear, and the service easy to use. However, in some cases it can take two weeks now before we get a business response. This is what we call a “long running” business flow.

![img](https://imgopt.infoq.com/fit-in/1200x2400/filters:quality(80)/filters:no_upscale()/articles/events-workflow-management/en/resources/3fig7-1513673352872.png)

## Implementing persistent state for long running services

Long running services need to keep persistent state somehow. In our case we have to remember that the payment is not yet retrieved, but also that the order is not yet fulfilled and in a state of waiting for that payment. These states have to survive system restarts. Of course, handling persistent state is not a new problem, and there are two typical solutions for it:

- Implement your own persistent “thing” e.g. Entity, Persistent Actor, etc
  - Ask yourself if you have ever built an order table with a column called status? There you are
- Leverage a state machine or workflow engine.
  - There are quite a few tools and frameworks on the market, some of which are very mature. Recently there was also several innovations within this space e.g. [Netflix](https://github.com/Netflix/conductor) and [Uber](https://github.com/uber/cadence) developing their own open source projects.

![img](https://imgopt.infoq.com/fit-in/1200x2400/filters:quality(80)/filters:no_upscale()/articles/events-workflow-management/en/resources/2fig8-1513673353009.png)

From our own experience we can report that implementing your own persistence mechanism for state handling often leads to home-grown state machines. This is due to the fact that you often face subsequent requirements such as timeout handling (“hey, let’s add a scheduler to the game”), visibility and reporting (“why can’t the business folks just use SQL to query the information?”), or monitoring operations if something goes wrong (“hmmm”).

The reason why it is so common to implement your own state machine is not only because of the “Not-Invented-Here” syndrome, but also because of conceptions around workflow and old-fashioned BPM tools in the market. Many developers had painful experiences with tools which were typically positioned as “zero-code”. Such tools were sold to business departments with the idea of getting rid of developers, which of course has not yet happened. Instead, the tools were handed over to IT departments and remained “alien” there. These tools were often heavyweight and proprietary, and developers experience what we call “death-by-properties-panel”.

## Lightweight state machines and workflow engines

Lightweight and flexible business process engines do exist, and can be used like every other library  -- with very few lines of code. They position themselves not as “zero-code”, but as a tool in the developer’s toolbox. They solve the hard problems of state machines and the pay-off is often seen early in many projects.

![img](https://imgopt.infoq.com/fit-in/1200x2400/filters:quality(80)/filters:no_upscale()/articles/events-workflow-management/en/resources/3fig9-1513673353142.png)

Such tools allow you to define flows either graphically with the ISO-Standard [BPMN](http://www.bpmn.org/), or with other flow languages often based on JSON, YAML or language dependant DSL’s (e.g. Java or Golang). One important aspect is that the flow definitions are effectively source code, as they will be executed directly. Executing means that the state machine knows how to transition from one state to the other.

![img](https://imgopt.infoq.com/fit-in/1200x2400/filters:quality(80)/filters:no_upscale()/articles/events-workflow-management/en/resources/1fig10-1513673353747.png)

Mature flow languages like BPMN offer quite powerful concepts, for example, handling time and timeouts, or sophisticated business transactions. Because there are so many projects leveraging BPMN, we know that we can tackle even tricky requirements with it.

![img](https://imgopt.infoq.com/fit-in/1200x2400/filters:quality(80)/filters:no_upscale()/articles/events-workflow-management/en/resources/2fig11-1513673354046.png)

In the above example workflow instances wait for a Goods Fetched event, but only until a certain timeout triggers. If that happens the business transaction is compensated -- meaning all compensating activities are executed, and  in this case the payment will be refunded. The state machine keeps track of already executed activities and therefore is able to trigger all necessary compensating actions. This allows the state machine to coordinate business transactions — the underlying idea is also referred to as the [Saga pattern](http://microservices.io/patterns/data/saga.html).

Leveraging graphical notations to define such flows also adds to the idea of living documentation -- documentation aligned to your running system in such a way that it cannot become out of sync with the actual behaviour. Some tools provide special support for unit testing certain scenarios including long running behavior. In [Camunda](https://camunda.org/) for example, every test run generates an HTML output which highlights the executed scenario, something that can be easily hooked into normal continuous integration (CI) reports. This way the graphical model adds even more value:

![img](https://imgopt.infoq.com/fit-in/1200x2400/filters:quality(80)/filters:no_upscale()/articles/events-workflow-management/en/resources/2fig12-1513673354211.png)

## Workflows live inside service boundaries

A very important concept is that using business workflow frameworks and tooling is a decentralised decision that is made by each and every service team. The state machine should be an implementation detail that is not visible externally from a service. There is no need for any central workflow tool, and the state machine should just be a library that is used to enable the long running behavior of some of your services more easily.

![img](https://imgopt.infoq.com/fit-in/1200x2400/filters:quality(80)/filters:no_upscale()/articles/events-workflow-management/en/resources/1fig13-1513673354500.png)

Another way of looking at this is that such a workflow engine is a logical part of your service. Depending on the tool of your choice, it can either run embedded into your application process (e.g. using Java, Spring and Camunda), as a separate process using simple language clients (e.g. using Java or Go and [Zeebe](https://zeebe.io/)) or used by a REST API (e.g. using Camunda or Netflix Conductor). Having this infrastructure available frees services from the burden of implementing state handling themselves in order to concentrate and focus on the business logic. You can design good service APIs and really smart endpoints because you can easily decide to make services potentially long running.

![img](https://imgopt.infoq.com/fit-in/1200x2400/filters:quality(80)/filters:no_upscale()/articles/events-workflow-management/en/resources/1fig14-1513673353567.png)

## Some code please

To ensure we are not only discussing these concepts theoretically, we have developed a small sample application showing all these concepts in action. The running code is available on [GitHub](https://github.com/flowing/flowing-retail/). We have used Java and only open source components (Spring Boot, Camunda, [Apache Kafka](https://kafka.apache.org/)) so that it is very easy to explore and experiment with.

![img](https://imgopt.infoq.com/fit-in/1200x2400/filters:quality(80)/filters:no_upscale()/articles/events-workflow-management/en/resources/1fig15-1513673354393.png)

## Wrap-Up

With a complex topic like business workflow modelling we could only scratch the surface of this topic by challenging some common hypotheses. Here are the key takeaways:

- Do events decrease coupling? Only sometimes! To be honest, events are great for decentralised data-management, generating read-models or tackling cross-cutting concerns. However, you should not implement complex peer-to-peer event chains. If you are tempted to do this then make sure that you send commands and are not afraid of orchestrating some other services.
- Does centralised control need to be avoided? Only somehow! We agree that centrally managed ESBs don’t fit into a microservices architecture. Smart endpoints and dumb pipes are preferable. However, don’t forget that all important business capabilities need a home. If you design smart endpoints you will not end up with bloated god services. Smart endpoints will often mean that you will have potentially long running services which internally handle all business problems that they are responsible for.
- Are workflow engines painful? Only some of them! In the past, BPM and workflow engines have been over-hyped concepts that were very vendor-driven, and so there are many horrible “zero-code” tools within the market. However, lightweight and easy-to-use frameworks exist, and most of them are even open source. They can run in a decentralised fashion, and can solve some hard developer problems. Don’t invest time in writing your own state machines, and instead leverage existing tools.

## About the Authors

**![img](https://imgopt.infoq.com/fit-in/1200x2400/filters:quality(80)/filters:no_upscale()/articles/events-workflow-management/en/resources/1Bernd-Rucker-1513673354608.jpg)Bernd Rücker** has coached countless real-life software projects to help customers implementing business logic centered around long running flows. Examples include the order process of the rapidly growing start-up Zalando selling clothes worldwide and the provisioning process for SIM cards at several big telecommunication organisations. During that time he contributed to various open source workflow engines. Bernd is also an author of the best-selling book Real-Life BPMN and co-founder of Camunda. He is totally enthusiastic about how flows will be implemented in next generation architectures.

**![img](https://imgopt.infoq.com/fit-in/1200x2400/filters:quality(80)/filters:no_upscale()/articles/events-workflow-management/en/resources/1Martin-Schimak-1513673353855.jpg)Martin Schimak** has been working for over a decade in complex domains like energy trading, telecommunication or wind tunnel organization. As a coder, he has a soft spot for readable and testable APIs, and enjoys working with sophisticated but lean state machines and process engines. As a domain “decoder”, Martin is into Domain-Driven Design and integration methods which shift his focus from technology to the user value of what he does. He is a contributor to several projects on GitHub, and speaks at meetups and conferences like ExploreDDD, O'Reilly Software Architecture Conference and KanDDDinsky. He blogs at[ ](http://plexiti.com/)[plexiti.com](http://plexiti.com/) and in his hometown Vienna he organizes meetups around Microservices and DDD.

