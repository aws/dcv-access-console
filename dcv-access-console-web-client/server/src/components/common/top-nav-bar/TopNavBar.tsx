// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

'use client';

import * as React from "react";
import TopNavigation from "@cloudscape-design/components/top-navigation";
import {service} from "@/constants/service-constants";
import {topNavBarConstants} from "@/constants/top-nav-bar-constants";
import {signOut} from "next-auth/react";
import {Session} from "next-auth";
import {useEffect, useState} from "react";
import {useRouter} from "next/navigation";

export default function TopNavBar({session}: { session: Session }) {
    const [serviceLogoExists, setServiceLogoExists] = useState(false);
    const {push} = useRouter();

    useEffect(() => {
        const img = new Image();
        img.onload = () => setServiceLogoExists(true);
        img.onerror = () => setServiceLogoExists(false);
        img.src = service.nameImage;
    }, []);

    return (
        <TopNavigation
            identity={{
                href: "/sessions",
                logo: serviceLogoExists ? {
                    src: service.nameImage,
                    alt: service.serviceName
                } : undefined,
                title: serviceLogoExists ? undefined : service.serviceName
            }}
            utilities={[
                {
                    type: "menu-dropdown",
                    text: session?.user?.name,
                    description: session?.user?.id,
                    iconName: "user-profile",
                    items: [
                        {id: "signout", text: topNavBarConstants.utilities.signOut}
                    ],
                    onItemClick: async (event) => {
                        if (event.detail.id === "signout") {
                            await signOut({
                                redirect: false
                            })

                            const logout_uri = session.logout_endpoint
                            if (logout_uri) {
                                const url = `${logout_uri}?client_id=${session.client_id}&response_type=code&logout_uri=${encodeURIComponent(session.post_logout_uri)}`
                                push(url)
                            }
                        }
                    }
                }
            ]}
            i18nStrings={{
                searchIconAriaLabel: topNavBarConstants.i18nStrings.searchIconAriaLabel,
                searchDismissIconAriaLabel: topNavBarConstants.i18nStrings.searchDismissIconAriaLabel,
                overflowMenuTriggerText: topNavBarConstants.i18nStrings.overflowMenuTriggerText,
                overflowMenuTitleText: topNavBarConstants.i18nStrings.overflowMenuTitleText,
                overflowMenuBackIconAriaLabel: topNavBarConstants.i18nStrings.overflowMenuBackIconAriaLabel,
                overflowMenuDismissIconAriaLabel: topNavBarConstants.i18nStrings.overflowMenuDismissIconAriaLabel,
            }}
        />
    );
}
