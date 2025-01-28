// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

export function getNativeOsName() {
    if (window?.navigator?.userAgent?.indexOf("Windows") != -1) {
        return "Windows"
    }
    if (window?.navigator?.userAgent?.indexOf("Mac") != -1) {
        return "macOS"
    }
    if (window?.navigator?.userAgent?.indexOf("Linux") != -1) {
        return "Linux"
    }
    return "Native"
}