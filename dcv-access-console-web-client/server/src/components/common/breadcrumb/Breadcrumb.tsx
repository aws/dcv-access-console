// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {BreadcrumbGroup} from "@cloudscape-design/components";
import {usePathname, useRouter} from "next/navigation";

const SEPARATOR = "/"

function formatPath(path) {
    return path.charAt(0).toUpperCase() + path.slice(1)
}

function getBreadcrumbItems(pathname, id, name) {
    const pathArray = pathname.split(SEPARATOR)
        .filter(v => v.length > 0)
    const formattedPathArray = pathArray[0] === "admin" ? [pathArray.slice(0, 2).join(SEPARATOR)].concat(pathArray.slice(2, pathArray.length)) : pathArray
    return formattedPathArray.map((subPath, idx) => {
        const href = SEPARATOR + formattedPathArray.slice(0, idx + 1).join(SEPARATOR)

        if (subPath.startsWith("admin/")) {
            subPath = subPath.split("admin/")[1]
        }

        if (subPath === id) {
            subPath = name
        } else {
            subPath = formatPath(subPath.replace(/([a-zA-Z])(?=[A-Z])/g, '$1 ').toLowerCase())
        }
        return {text: subPath, href: href}
    })
}

export type BreadcrumbProps = {
    id?: string | undefined
    name?: string | undefined
}

export default function Breadcrumb({id, name}: BreadcrumbProps) {
    const router = useRouter();
    const pathname = usePathname();

    return (
        <BreadcrumbGroup
            items={getBreadcrumbItems(pathname, id, name)}
            ariaLabel="Breadcrumbs"
            onFollow={event => {
                event.preventDefault();
                router.push(event.detail.href)
            }}
        />
    );
}
