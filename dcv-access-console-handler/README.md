# DCV Access Console Handler
This component handles the requests from the front end of the Web Client
and sends them to the [(DCV Session Manager Broker)](https://docs.aws.amazon.com/dcv/latest/sm-admin/what-is-sm.html).
This package is a Java library package using Gradle.

## Build
Build locally with `./gradlew release` to pull dependencies from Maven Central.
To start the application, run `./gradlew bootRun`.
   
## Setting up the datastore
While most data resides on the broker, `SessionTemplate` and related data resides on the Handler.
For persistence, SQL based datastores and DynamoDB are supported. The following configurable properties reside in `access-console-handler.properties`:
* `persistence-db` = `mysql`/`dynamodb` to select datastore type
* `hibernate-ddl-auto` = `update` enables making DDL changes to the MySQL db. To disable, set to `none`

## Reading claims from the auth server
When using external OAuth providers like Cognito, user properties like login username (username used by Session Manager) and display name can be extracted from the userInfo endpoint.
The following properties can help with this:
* `jwt-login-username-claim-key` is the key for the login username claim key
* `jwt-display-name-claim-key` is the key for the display name claim key
* `auth-server-well-known-uri` is the well known URI (required only if userInfo endpoint is not provided)
* `auth-server-userinfo-endpoint` is the userInfo endpoint

## Testing
While running the application you can use Postman to send requests to the endpoints to ensure correct functionality.

## Changing Java Server
Spring supports using either a Tomcat or Jetty server as the backend. Currently, the project is configured to use Jetty.
To change to Tomcat, simply comment out the line excluding Tomcat in the `build.gradle.kts` file: 

```exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")```

## Metrics
* [spring-actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html) is used to provide health formation and control logging for the server.
  * See http://localhosts:8080/actuator/health for the health info
  * See http://localhost:8080/actuator/metrics/http.server.requests for request info
  * See http://localhost:8080/actuator/metrics/logging for current log levels and post request to update them 
* This works in conjunction with [micrometer](https://micrometer.io/) to publish the metrics to cloudwatch or another supported metric datastore

## Generating a self-signed cert and a keystore. Enabling TLS
1. Create a CA
    2. ```aidl
       openssl genrsa -des3 -out rootCA.key 2048
       openssl req -x509 -new -nodes -key rootCA.key -sha256 -days 1825 -out rootCA.pem
2. Generate a server cert
    1. ```aidl
      openssl genrsa -des3 -out server.key 2048
      openssl req -new -sha256 -key server.key -out server.csr
      ```
3. Sign the server cert with the CA
    1. ```aidl
      openssl x509 -req -in server.csr -CA rootCA.pem -CAkey rootCA.key -CAcreateserial -out server.pem -days 365 -sha256
      ```
4. Create a keystore with the cert and private key
    1. ```aidl
      openssl pkcs12 -export -in server.pem -out keystore.p12 -name server -nodes -inkey server.key
      ```
5. Update the `server.ssl.*` values in the properties file

## Logging
Logging configuration is handled by `src/main/resources/logback-spring.xml` which overrides all logging properties in `access-console-handler.properties`.

## References
* Gradle User Guide Command-Line Interface (options you can use with `./gradlew`): https://docs.gradle.org/current/userguide/command_line_interface.html
* Gradle User Guide Java Plugin: https://docs.gradle.org/current/userguide/java_plugin.html
* Gradle User Guide Checkstyle Plugin: https://docs.gradle.org/current/userguide/checkstyle_plugin.html
* Gradle User Guide SpotBugs Plugin: http://spotbugs.readthedocs.io/en/latest/gradle.html
* Gradle User Guide JaCoCo Plugin: https://docs.gradle.org/current/userguide/jacoco_plugin.html
* Authoring Gradle Tasks: https://docs.gradle.org/current/userguide/more_about_tasks.html
* Executing tests using JUnit5 Platform: https://junit.org/junit5/docs/current/user-guide/#running-tests-build-gradle and https://docs.gradle.org/4.6/release-notes.html#junit-5-support

## Learn more
* https://docs.spring.io/spring-boot/docs/2.0.x/reference/html/production-ready-metrics.html
* https://docs.spring.io/spring-boot/docs/current/actuator-api/htmlsingle/#loggers