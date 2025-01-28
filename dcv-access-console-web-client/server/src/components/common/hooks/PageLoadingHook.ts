// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {useSession} from "next-auth/react";
import {useEffect, useState} from "react";

export default function usePageLoading() {
    const {data: userSession, status} = useSession({
        required: true,
    })
    const [loading, setLoading] = useState(true)
    useEffect(() => {
        if(status !== "loading") {
            setLoading(false)
        }
    }, [status])
    return {loading, userSession};
}
