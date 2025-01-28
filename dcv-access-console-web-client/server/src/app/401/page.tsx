// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

'use client';
import Error from "@/components/common/error/Error";

export default function ErrorPage() {
    return (
        <Error title={"401 Unauthorized"} confirmPath={"/"}/>
    );

}
