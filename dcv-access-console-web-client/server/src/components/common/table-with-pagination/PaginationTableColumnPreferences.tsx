// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {CollectionPreferencesProps} from "@cloudscape-design/components";

export const DEFAULT_PAGE_SIZE_PREFERENCES: CollectionPreferencesProps.PageSizePreference = {
    title: "Page size",
    options: [
        { value: 5, label: "5 resources" },
        { value: 10, label: "10 resources" },
        { value: 20, label: "20 resources" },
        { value: 50, label: "50 resources" },
        { value: 100, label: "100 resources" }
    ]
}