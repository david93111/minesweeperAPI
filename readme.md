# minesweeperAPI

Classic Minesweeper game designed as a microservice using Akka HTTP with Circe for API exposure
on conjunction with Akka Actors and Akka Persistence over MongoDB as Journal and Snapshot store 
to represent and store games with event sourcing for game movements history, games can be 
created with up to a 30 X 30 board with the desired quantity of mines distributed randomly across
the board, time tracking, game pausing and resuming and also field marks features are present.

### Why this stack ?

Akka HTTP is a great library for building expressive REST APIs, due to is not a framework (not intend to by the creators), 
is a library that seeks to give the required and fundamentals tools to build on top of the already implemented directives or two
build own custom directives that accomplish the need behavior, also allow to build an idiomatic and low-level lightweight API 
not focused on interaction with the browser directly to render content, for this reasons fit perfect to build RESTful simple and readable 
APIs with a great concurrent system built on top of stream and with the combination of circe one of the best and more efficient json management library for scala is posible to write a
totally type safe API with Entity fast coding and decoding 
 
Actors a great way to represent state, fit great also for encapsulate the time tracking feature and
support high load with a top notch concurrent model avoiding all the dangers of parallelism cause actor is isolated
and only accessible through mailbox, so guarantee state preservation and correct transitions. 

This benefits combined with event sourcing and a really fast and reliable NoSQL data source as Mongo and the reactive
plugin for persistence that brings use of streams to write and read data with confidence over a the great back pressure implementation of
akka streams, brings a stack that can gain a really good performance with great guaranties of storing state plus future extensibility, 
and also a way to failure recovery or recover previous state and no only the last know photo, but full historic over an entity, 
which in a game comes great to see step by step whats was happening inside the mind of the player or what could have been changed to win. 

### Client Library
There is a client for the API build with JS using ES6 standard and Axios as HTTP Client 
is designed to run as an NPM module or in the browser, can be found on the following link

##### https://github.com/david93111/minesweeper-client 

### Quick Start

#### Try the web API right away

To try the API or play right away there is a version already deployed on heroku 
of the application on the following URL
##### https://minesweeper-akka-api.herokuapp.com/minesweeper

if you want to gave a look to how consume the API here is a postman documentation
with the postman collection import option or curl prebuilt requests and service explanations, only change the data
as you want and your ready to go
##### https://documenter.getpostman.com/view/1567366/RWaGTUsv

__*NOTE:*__ The first request can take longer due to the fact that the application is deployed on a heroku free dyno, 
this means that the applications is on "Sleep Mode" (Lets call it that way) if no traffic is received.

### Run with Docker
There is a version of the MineSweeper API using Docker, does not include a MongoDB, so you will need to connect the 
container with the MongoBD if is in your local either directly installed or using docker too.
To run the image you can use the following commands:
*Note that you need to set the MONGO_URL variable based on the location of you MONGODB and credentials 
 ```bash
 docker run --name some-minesweeper -e MONGO_URL=MyMongoHost david9311/minesweeper-akka-api
 ```

#### Build image
To build the image from scratch with the current version of your source code you can use the following command inside
the root folder of the project:
```bash
docker build -t minesweeper-akka-api:latest .
```
Take in mind that you must need to create a distribution first using the following command inside the root folder of the
project also:
```bash
sbt dist
```
Now you can execute your own image
```bash
docker run -rm --name some-minesweeper minesweeper-akka-api:latest
```

#### Test locally 

##### Prerequisites to run on machine

For running the application you will need to have an scala full

* JDK 1.8+ installed
* SBT for compile, test, coverage and building
* Scala 2.12+ (for development and build)
* MongoDB 3.6+ for persistent actors, consider use the [MongoDB official docker image](https://hub.docker.com/r/_/mongo/)

*__NOTE:__* there is a pre-release version stable (v0.0.1) with less features (No pause, resume, no time tracking) 
that supports only in memory persistence of actors, if you want to avoid the install of a MongoBD 
you can try that version, since version 1.0.0+ Persistence Actor is supported, you can use a Docker
version of Mongo to play with te latest version
##### Running source code
1. Clone the repository 
    ```bash
    git clone https://github.com/david93111/minesweeperAPI.git
    ```
2. go to root of the repository 
3. execute commnand
    ```bash
        sbt reStart
    ```
    This will launch the akka-http with the configured port defined on the application.conf
    inside the main resources folder, default is 
    ```
         http{
            host = "0.0.0.0"
            port = 8010
         }
    ```

##### Running distribution

To run a distribution you can generate it through the source code or download the 
available distribution .zip at release v1.0.0

To generate a build artifact:
1. Go to root folder of project
2. execute the following command 
   ```bash
   #this will generate an executable for unix or windows system
   sbt clean stage 
   
   # to generate the a zip directly use 
   sbt clean dist
   ```
3. go to the folder target/universal/minesweeperms/bin inside the root of the project
4. Execute the file based on your operative system (.bat for windows, sh for unix based systems)

If you downloaded the zip file, just extract its content, and execute the fourth step
inside the bin folder of the extracted zip

### How to use the API

inside the project is the Postman collection file that has all the services available on 
the API with an example on how must be consumed, the services are self explained and also documented on the code
but also each service is documented on this public link with examples:
##### https://documenter.getpostman.com/view/1567366/RWaGTUsv

### Built With

* Akka HTTP for API exposure
* Circe with Akka HTTP Circe for Marshalling of objects to achieve typed API 
* Akka Actors to manage state of game
* Akka Persistence to persist state of game actors and game state history as event sourcing
* Akka Kryo for serialization of messages to be persisted as java serializer is not a good option in many cases (Useful for clustering also)
* Reactivemongo Persistence Plugin as a reliable connector to MongoDB for data storing and stream persistence query
* InMemory Persistence Plugin For testing purposes of persistence process
* Scalatest + AkkaTestkit + RouteScalaTest for unit test
* Sbt Coverage and Sbt Native Package Plugins for test coverage and packaging

### Test and Coverage
The project is build with unit test for all the operation within the API or Actors including persistence
process using Akka Persistence in memory plugin, the test are complemented with the coverage plugin,
the coverage is expected to be always over 80%, there is an additional application.conf file inside the 
test resources to override some configurations on the app for test execution.

##### Testing the project
In order to execute the tests SBT is needed as well as Scala in the specified versions on the pre-requisites version.

For launch the test process, execute the following command inside the root folder
````bash
sbt clean coverage test
# or sbt test if coverage not needed later
```` 
This command will clean previous instrument data of coverage if the test where ran previously, will set
the coverage for the following commands and the execute the unit tests, for now no IT tests are present as there is no
CI or CD processes for this project for now

##### Generate coverage report
once the test are finished successfully, if you want to verify the coverage based on the last test execution, use the
following command:
````bash
# Note that launching test with coverage is needed to make a useful report
sbt coverageReport
```` 
this will generate an XML report inside target/scala-{scalaversion}/coverage-report folder and an 
HTML report inside target/scala-{scalaversion}/scoverage-report folder, 
open the index.html it using your browser to see the result, segmented by package and class.

Take if mind that once you execute the clean command again on SBT all the target data is cleaned up and
the report will be lost, so save it outside if you want to compare it later


### License 

This project is licensed under the MIT License - see the [LICENSE](https://github.com/david93111/minesweeperAPI/blob/master/LICENSE) 
file for details