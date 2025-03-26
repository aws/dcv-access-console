# DCV Access Console Integration Tests

This package contains the integration tests for the DCV Access Console project.

## Prerequisites
1. Have an Access Console setup in an AWS account that is integrated with Session Manager
2. Create SSM parameters with `/integTestingSecrets/authId/<suffix>` and `/integTestingSecrets/authSecret/<suffix>`
   that contains the client id and client secret that the integration tests can use 
3. Update the `testing.xml` file to match the `instanceName`, `securityGroupNamePrefix` and suffix.

## Running the tests
`./gradlew run-integration`
