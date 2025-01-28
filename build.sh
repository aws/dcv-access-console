#!/usr/bin/env bash
# Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

set -e

# If VERBOSE is set, enable -x.
if [ -n "$VERBOSE" ]
then
    set -x
fi

echo "Building the handler..."
cd dcv-access-console-handler
./gradlew release

echo "Building the web client..."
cd ../dcv-access-console-web-client/server
npm install
npm run build

echo "Building the configuration wizard..."
cd ../../dcv-access-console-configuration-wizard
./build.sh

echo "Building the integration tests..."
cd ../dcv-access-console-integration-tests
./gradlew release