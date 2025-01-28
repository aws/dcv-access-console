// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import Box from "@cloudscape-design/components/box";

export const ValueWithLabel = ({ label, children }) => (
    <div>
        <Box variant="awsui-key-label">{label}</Box>
        <div>{children}</div>
    </div>
);