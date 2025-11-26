## DCV Access Console
This package contains the source code of the DCV Access Console.

## Background
[Amazon DCV](https://aws.amazon.com/hpc/dcv/) is a high-performance remote display protocol that enables secure delivery of remote desktops and application streaming from any cloud or data center to any device, even over varying network conditions. It allows graphics-intensive applications to run remotely on EC2 instances, streaming their user interfaces to simpler client machines. This eliminates the need for expensive dedicated workstations and is widely used across various high-performance computing (HPC) workloads.

[Amazon DCV Session Manager](https://docs.aws.amazon.com/dcv/latest/sm-admin/what-is-sm.html), a component of the DCV ecosystem, consists of installable software packages (an Agent and a Broker) and an API to programmatically create and manage Amazon DCV sessions across a fleet of Amazon DCV servers.

## DCV Access Console Overview
The [DCV Access Console](https://docs.aws.amazon.com/dcv/latest/access-console/what-is-access-console.html) is a GUI interface for DCV Session Manager that helps administrators and end users manage their Amazon DCV sessions. The Access Console consists of installable software packages that include a Handler, an Authentication Server, a Web Client, and a Configuration Wizard configured to provide a graphical interface.

### Architecture
```
+------------------------+----------------------------------------+----------------------------+
|  End user managed      |  Amazon DCV Access Console             | Session Manager           |
|  space                 |  managed space                         | managed space             |
|                        |                                        |                           |
|                        |                                        |                           |
|   +-------------+      |  +------------+      +----------+      |  +--------+               |
|   |             |      |  |            |      |          |      |  |        |               |
|   | Web Browser | <--> |  | Web Client | <--> | Handler  | <--> |  | Broker |               |
|   |             |      |  |            |      |          |      |  |        |               |
|   +-------------+      |  +------------+      +----------+      |  +--------+               |
|                        |        ^                 ^             |                           |
|                        |        |                 |             |                           |
|                        |        |    +----------------+         |                           |
|                        |        |    | Authentication |         |                           |
|                        |        +--> |    Server      |         |                           |
|                        |             +----------------+         |                           |
|                        |                                        |                           |
|                        |                                        |                           |
|                        |                                        |                           |
+------------------------+----------------------------------------+----------------------------+
```
This repository contains the following components, each with its own detailed README containing build and setup instructions.

* [Handler](https://github.com/aws/dcv-access-console/blob/main/dcv-access-console-handler/README.md): Handles connections to and manages Amazon DCV sessions by communicating with the [Session Manager Broker](https://docs.aws.amazon.com/dcv/latest/sm-admin/what-is-sm.html) using the Session Manager APIs.
* [Authentication Server](https://github.com/aws/dcv-access-console/blob/main/dcv-access-console-auth-server/README.md): Manages user authentication.
* [Web Client](https://github.com/aws/dcv-access-console/blob/main/dcv-access-console-web-client/README.md): Provides the user interface for session management which interacts with the Handler.
* [Configuration Wizard](https://github.com/aws/dcv-access-console/blob/main/dcv-access-console-configuration-wizard/README.md): Script for creating the four configuration files used by the DCV Access Console, as well as creating a self-signed cert for the WebServer.
* [Model](https://github.com/aws/dcv-access-console/blob/main/dcv-access-console-model/README.md): Swagger API model for communication between the Web Client and Handler components.
* [Integration Tests](https://github.com/aws/dcv-access-console/blob/main/dcv-access-console-integration-tests/README.md): End-to-end testing suite.

### Prerequisites
Before setting up the Amazon DCV Access Console, you must first install and configure the Session Manager Agent and Broker. For more information about setting up Amazon DCV Session Manager, see the [Amazon DCV Session Manager Administrator Guide](https://docs.aws.amazon.com/dcv/latest/sm-admin/what-is-sm.html).

### Requirements
* Supported operating systems include Amazon Linux 2, AL 2023, RHEL 9.x, Rocky Linux, and Ubuntu
* 64-bit architecture (x86 or ARM)
* Minimum 4GB memory per component
* Java 17 (Authentication Server and Handler)
* Node.js 16 (Web Client)
* Datastore for the Handler: DynamoDB (requires an AWS account), MySQL or MariaDB

For complete system requirements please refer to our [requirements documentation](https://docs.aws.amazon.com/dcv/latest/access-console/requirements.html).

### Build
Run the `build.sh` script to trigger a build for all the components. Alternatively, the components can be built individually by following the instructions in their specific READMEs.

### Setting up AWS Cognito for external oAuth
1. Go to Amazon Cognito on the AWS Console -> User pools -> Create user pool
2. Set up resources for your application and Create user directory:
   * Define your application- Traditional web application 
   * Configure options as you like
   * Add a return URL: <web-client-url>/api/auth/callback/<NEXT_PUBLIC_SM_UI_AUTH_ID>. For example, using defaults for a locally running server: http://localhost:3000/api/auth/callback/dcv-access-console-auth-server
   * Once the user pool is created, you can configure Allowed sign-out URLs: Applications -> App clients -> Login pages -> Managed login pages configuration -> Edit
3. Adding users to the user pool:
   * Go to User management -> Users and add users 
   * Alternatively, if you have allowed self-registration in step 2, users may sign up themselves
4. Preparing the handler:
   * Copy the User pool ID from the user pool Overview page and set `jwt-issuer-uri` as https://cognito-idp.<region>.amazonaws.com/<user_pool_id>
   * Set the following properties:
     * `jwt-login-username-claim-key` is the key for the login username claim key
     * `jwt-display-name-claim-key` is the key for the display name claim key
     * `auth-server-well-known-uri` is the well known URI (required only if userInfo endpoint is not provided)
     * `auth-server-userinfo-endpoint` is the userInfo endpoint
5. Preparing the web client:
   * Set the `auth-server-well-known-uri` in the format https://cognito-idp.<region>.amazonaws.com/<user_pool_id>/.well-known/openid-configuration
   * Set the auth server client ID and secret values to match the values of the user pool App client you set up in step 2 above
   * Set `SM_UI_AUTH_ENABLE_PROVIDER_LOGOUT` as true to enable signing out of Cognito on each log out event
   * Leave `SM_UI_AUTH_LOGOUT_URI` empty since `end_session_endpoint` from well_known json will be used to log out of Cognito


## Getting Help
AWS provides support for the Access Console in its default, unmodified state. Your existing support model will extend to include support for the Access Console. If you have made custom modifications or built additional features on top of the Access Console, AWS will not be able to provide support for these customized elements.

The best way to interact with our team is through GitHub. You can open an [issue](https://github.com/aws/dcv-access-console/issues/new/choose).

If you have a support plan with AWS Support, you can also create a new [support case](https://support.console.aws.amazon.com/support/home#/).

## Known Issues
### Security Vulnerabilities
There are known security vulnerabilities that cannot be addressed at this time due to Node.js version constraints. The Access Console requires Node.js v16 to support Amazon Linux 2 (AL2).

## Security
See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License
This project is licensed under the Apache-2.0 License. The components in this repository - Access Console Web Client, Authentication Server, Handler, and Configuration Wizard - may be used with additional Amazon DCV components, like the DCV clients, governed by the DCV EULA and made available for download on https://www.amazondcv.com/.
If you choose to consume these components as a published build from that web page, instead of as open sourced components from here, that build will also be governed by the DCV EULA.