# DCV Access Console Web Client
* This is a [Next.js](https://nextjs.org/) project bootstrapped with [`create-next-app`](https://github.com/vercel/next.js/tree/canary/packages/create-next-app).
* [Storybook](https://storybook.js.org/) is used as the frontend workshop for building UI components and pages in isolation 
* [Cloudscape](https://cloudscape.design/) is used as the design system and its components are used to build pages
* [Tailwind](https://tailwindcss.com/) is used as the CSS framework

## Project Structure
The project is structured as follows:

```text
dcv-access-console-web-client/server
├─src/
| ├─app/ # Holds all the different pages of the site. Routing is based on the file structure
| | ├─route.ts # Root page
| | ├─layout.tsx # Root layout
| | ├─globals.css # Style sheet applied to all pages
| | ├─<page>/ # Folder for the page
| | | └─route.ts # Page that is display when user goes to /<page>/
| ├─components/ # React components
| | ├─<grouping-of-components>/ # Components related to a page or function
| | | ├─<component-name>/ # Folder that contains all the component files
| | | | ├─<component-name>.ts|tsx # Code for the component
| | | | ├─<component-name>.stories.tsx # Stories for the story book for the component
| | | | └─<component-name>.test.tsx # Tests for the component
| ├─constants/ # Common constants
| | └─<constant-group>.ts
```

## Add logos
The Web Client renders some logos on the UI which can be added to the repository for a more desirable experience. The files that should be added are as follows:
1. public/linux-logo.svg
2. public/windows-logo.svg
3. public/service-name.svg
4. src/app/favicon.ico

## Build
```bash
cd server
npm install
npm run build
```

To run tests and get coverage report run:
```bash
cd server
npm run test
```

## Setting up the development server
Ensure that the auth-server is running and set the following variables in `.env.development`:
1. SM_UI_HANDLER_BASE_URL - Change the `<replace>` to the URL of the Handler, with the port
2. SM_UI_AUTH_WELL_KNOWN_URI  - Change the `<replace>` to the URL of the Auth Server, with the port
3. SM_UI_AUTH_CLIENT_ID - Should match the `client-id` of the Auth Server
4. SM_UI_AUTH_CLIENT_SECRET - Should match `client-secret` of the Auth Server, without `{noop}`
5. NEXT_PUBLIC_SM_UI_AUTH_ID - Should match the ending of `redirect-uris` in the Auth Server `access-console-auth-server.properties` file
6. NEXT_PUBLIC_DEFAULT_PATH - Leave as `/sessions`
7. NEXTAUTH_SECRET - Set a string
8. NODE_EXTRA_CA_CERTS - Path to cert file
9. SESSION_SCREENSHOT_MAX_WIDTH and SESSION_SCREENSHOT_MAX_HEIGHT - Can be undefined or a value greater than or equal to 0. If set to 0, the resolution set in the Broker configuration will be used. If undefined, default values will be used.

## Running the development server
```bash
cd server
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

## Run the storybook server:
```bash
npm run storybook 
```
Open [http://localhost:6006](http://localhost:6006) with your browser to see the stories.

## Add a new component
1. Create a new folder in `kebab-case` naming convention with the name of the component
2. Create a new `.tsx` file with the component name, currently using `PascalCase` for naming files
3. Create an associated `*.stories.tsx`. The story should represent the different cases states of the components. Some examples, loading state, error state, valid state, filtered state etc.
4. Create an associated `*.test.tsx`. The tests should cover all lines/branches in the code and sufficiently cover all the use cases for the component

## Add new tests
1. Create file with name <component>.test.tsx
2. Add tests
   1. Mock any dependencies such as router using `jest.mock`
   2. Use `describe` to group tests
   3. Use `it` or `test` to write the tests
   4. Use `userEvent` to simulate events such as click
   5. Render the components from the associated story file to test different cases of the component
   6. The arguments from story can be used in the test

## Testing with the Handler
1. Update the `NEXT_PUBLIC_SM_UI_HANDLER_BASE_URL` variable in `.env.development` with the Handler host/ip and port
2. If you want to mock the Handler endpoint, set the `NEXT_PUBLIC_ENABLE_MOCK_WORKER` to `true` and set the `NEXT_PUBLIC_SM_UI_HANDLER_BASE_URL` to `http://localhost:8080`. This will start the mock server and the network calls from the browser are intercepted and [msw](https://mswjs.io/) responds with the mocked responses from `generated-src/mock.js`

## Generating a self-signed cert and setting up TLS
NOTE: Do not do this for dev environments (unless you really know what you're doing)
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
4. Install [NGNIX](https://www.nginx.com/resources/wiki/start/topics/tutorials/install/).
   1. For mac use can use homebrew `brew install nginx`
5. Update the NGNIX config file by commenting out all the server context inside the http context and replacing it with
   ```aidl
       server {
           listen       443 ssl;
           server_name  <SM_UI_WEBCLIENT_HOST_NAME>:<SM_UI_WEBCLIENT_PORT>;

           ssl_certificate      <PATH_TO_SSL_CERT>;
           ssl_certificate_key  <PATH_TO_SSL_CERT_KEY>;
    
           ssl_session_cache    shared:SSL:1m;
           ssl_session_timeout  5m;
    
           ssl_protocols       TLSv1.3;
           ssl_ciphers  HIGH:!aNULL:!MD5;
           ssl_prefer_server_ciphers  on;
    
           location / {
               proxy_pass   http://localhost:<SM_UI_WEBCLIENT_PORT>;
           }
        }
   ```
6. Start NGINX
   1. For mac `/usr/local/opt/nginx/bin/nginx -g daemon\ off\;`
7. In `.env.development` file add `NODE_TLS_REJECT_UNAUTHORIZED=0` if it is a self-signed cert
8. Start the webclient

## References:
* [Breadcrumb.test.tsx](./src/components/common/breadcrumb/Breadcrumb.test.tsx)
* [Jest Matchers](https://jestjs.io/docs/using-matchers)
* [Jest Expect](https://jestjs.io/docs/expect)
* [React Testing Library Tutorial](https://www.robinwieruch.de/react-testing-library/)

## Learn More
To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.
