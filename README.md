## DCV Access Console
The DCV Access Console is a web application that helps administrators and end users manage their Amazon DCV sessions.
The Access Console consists of installable software packages that include a Handler, an Authentication Server, a Web Client, and a Setup Wizard configured to provide a graphical interface.
This repository contains the source code for the Handler, the Web Client, and the Setup Wizard components currently.
The DCV release version of the Authentication Server can be obtained via [DCV Downloads](https://www.amazondcv.com/#:~:text=Amazon%20DCV%202024.0%20Access%20Console).

### Build
Run the `build.sh` script to trigger a build for all the components. Alternatively, the components can be built individually by following the instructions in their specific READMEs.

## Security
See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License
This project is licensed under the Apache-2.0 License. The components in this repository - Access Console Web Client, Handler, and Setup Wizard - may be used with additional Amazon DCV components, like the Authentication Server or DCV clients, governed by the DCV EULA and made available for download on https://www.amazondcv.com/.
If you choose to consume these components as a published build from that web page, instead of as open sourced components from here, that build will also be governed by the DCV EULA.