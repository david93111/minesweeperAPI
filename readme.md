# minesweeperAPI

Classic Minesweeper game designed as a microservice using Akka HTTP with Circe for API exposure
on conjunction with Akka Actors and Akka Persistence over MongoDB as Journal and Snapshot store 
to represent and store games with event sourcing for game movements history, games can be 
created with up to a 30 X 30 board with the desired quantity of mines distributed randomly across
the board, time tracking, game pausing and resuming and also field marks features are present.

a full list of features and how to use can be found below. So let's play !

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
with the postman collection import option or curl prebuilt requests, only change the data
as you want and your ready to go
##### https://documenter.getpostman.com/view/1567366/RWaGTUsv

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
the API with an example on how must be consumed, also is documented on a public section in postman
on this link, the services are self explained and also documented on the code 
##### https://minesweeper-akka-api.herokuapp.com/minesweeper

### Built With

* Akka HTTP for API exposure
* Circe with Akka HTTP Circe for Marshalling of objects to achieve typed API 
* Akka Actors to manage state of game
* Akka Persistence to persist state of game actors and game state history as event sourcing
* Akka Kryo for serialization of messages to be persisted (Useful for clustering also)
* Reactivemongo Persistence Plugin as a reliable connector to MongoDB for data storing
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
```` 
This command will clean previous instrument data of coverage if the test where ran previously, will set
the coverage for the following commands and the execute the unit tests, for now no IT tests are builded as there is no
CI CD processes for this project for now

once the test are finished, if you want to verify the coverage based on the last test execution, use the
following command:
````bash
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