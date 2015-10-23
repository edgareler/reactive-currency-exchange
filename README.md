# Reactive Currency Exchange

A client-server reactive application for currency exchange rate, created with Play! Framework, Java and MongoDB.

## How to run

- Clone this repository.
- Install and configure [MongoDB](https://www.mongodb.org) to listen the port **27017**.
- Open a shell and navigate to the project folder:
```sh
$ cd ./reactive-currency-exchange
```
- Import the database to create it:
```sh
$ mongoimport -d currency-exchange -c currency ./database.json
```
- Install [JDK 1.8+](http://www.oracle.com/technetwork/pt/java/javase/downloads/jdk8-downloads-2133151.html).
- Install [Play! Framework 2.4.x](https://www.playframework.com/documentation/2.4.x/Installing).
- Open a shell and navigate to the server application folder:
```sh
$ cd ./currency-exchange-server
```
- Start the activator:
```sh
$ activator
```
- Run the application (it will download all the dependencies):
```sh
[currency-exchange-server] $ run
```
- Open the address in a browser:
```
http://localhost:9000
```
- It will return a "Forbidden" (403) message. The server layer is running.
- Open a new shell and navigate to the client application folder:
```sh
$ cd ./currency-exchange-client
```
- Start the activator with the HTTP port 9001:
```sh
$ activator -Dhttp.port=9001
```
- Run the application:
```sh
[currency-exchange-client] $ run
```
- Open the address in a browser:
```
http://localhost:9001
```
- It will return the front-end of the application.
