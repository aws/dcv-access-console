# dcv-access-console-auth-server

This is an authorization server that uses [spring-boot-starter-oauth2-authorization-server](https://docs.spring.io/spring-authorization-server/docs/current/reference/html/getting-started.html). It is used as the authentication server for the DCV Access Console.

## TODO:
Add the spot checks back

## Setup
1. Update the `access-console-auth-server.properties` and `access-console-auth-server-secrets.properties` with the correct client-id, client-password and redirection urls to match with the dcv-access-console-webclient
2. Run using `bb bootRun` 
3. Header Based Authentication, this authentication method relies on an external authentication system to authenticate the users. The external system then adds an HTTP authentication header to the user requests. The Auth server trusts the HTTP authentication header.
   1. Add the following to the properties files:
   2. Change `<replace>` under the `redirect-uris` setting to the URL where your WebClient is running
   3. Change `<replace>` under the `post-logout-redirect-uris` setting to the URL where your WebClient is running
   4. For dev environment, install a chrome extension called [requestly](https://chrome.google.com/webstore/detail/requestly-open-source-htt/mdnleldcmiljblolnjhpnblkcekpdkpa).
      1. [Add a rule](https://docs.requestly.io/browser-extension/edge/getting-started/create-first-rule) to add a `Request Header` with name `username` and value `<your alias>`. To view as an admin, set the value to `admin`.
      2. When the URL contains where ever you are running the auth server, usually `localhost`
      3. This will simulate a proxy server on customer environment which will add the header field 

## Generating a self-signed cert and a keystore. Enabling TLS
1. Create a CA
   2. ```aidl
       openssl genrsa -des3 -out rootCA.key 2048
       openssl req -x509 -new -nodes -key rootCA.key -sha256 -days 1825 -out rootCA.pem
2. Generate a server cert
   1. ```aidl
      openssl genrsa -des3 -out server.key 2048
      openssl req -new -sha256 -key server.key -out server.csr
3. Sign the server cert with the CA
   1. ```aidl
      openssl x509 -req -in server.csr -CA rootCA.pem -CAkey rootCA.key -CAcreateserial -out server.pem -days 365 -sha256
4. Create a keystore with the cert and private key
   1. ```aidl
      openssl pkcs12 -export -in server.pem -out keystore.p12 -name server -nodes -inkey server.key
5. Update the `server.ssl.*` values in the properties file
