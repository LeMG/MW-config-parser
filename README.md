# Middleware config parser
Security auditting requires lots of repetitive works.(likes checking config values of server o/s or m/w)
This problem can be solved if you can install simple agent that reads and checks server config,
but in some case  it's not possible to install additional programs on server.
This project will support parsing several middleware config values.
## Built With
This project is built with following environment
* O/S - Android 7.0
* Java - [Openjdk 1.8](http://openjdk.java.net)
* Maven
* [Apache POI](https://poi.apache.org) - Used to make excel format output
## Installation
Compile this project with maven
```
mvn clean compile
```
## Usage
### Place configuration files in following directory structure.
```
service_name/
	┗ server_isntance/
		┗ m/w instance/
			┗ config files
```
※Currently it supports Apache httpd, Apache Tomcat, nginx.
※If there are unused files, result can be different.
### Execute jar file with following parameters
```
java -jar mwConfigparser.jar -p /{PATH_TO_SERVICE_DIR}/service_name
```
