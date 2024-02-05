# Properties Server

## Overview

### Assumptions
* Client-server authentication is out of scope. If needed, it can be implemented with mutual TLS (mTLS), or in a microservices design, an authentication/authorization reverse proxy in front of the server API.
* The server should handle messages sent by multiple clients simultaneously, so there is no guarantee that a client's message will not be overwritten by another client sending a message for the same filename. 
* Write throughput and performance is more important than a client receiving synchronous confirmation of a successful write.

### Design

The server exposes a REST API POST endpoint at /api/v1/file/name where `name` is the name of the properties file without the `.properties` extension. Only alphanumeric characters, `_`, and `-` are supported. Supported filename characters were designed to be a non-configurable allowlist, to avoid misconfigurations that can introduce security issues, such as allowing the character `.`, which can result in writing to a parent directory with `..` in Linux/Unix.

### HTTP response codes
* 202 Accepted: The request has been accepted for processing.
* 400 Bad Request: The request is invalid.
* 500 Internal Server Error: An unexpected server error occurred.

## How to run
1. Install `mvn` (Maven).
2. Create server.properties file:
```shell
server.port=8888
logging.level.io.github.anomal.propsserver=DEBUG
outputdir=/var/tmp/serveroutput
```
3. Go to repository root.
4. Run:

```shell
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.config.location=file:///path/to/server.properties
```
### Note
This was tested on Linux.

## Testing with curl
```shell
curl -v -H "Content-Type: application/json" -d '{ "http" : "80", "https" : "443" }' localhost:8888/api/v1/file/name
```
