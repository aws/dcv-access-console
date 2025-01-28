// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

'use client';

import * as React from "react";
import TopNavigation from "@cloudscape-design/components/top-navigation";
import {service} from "@/constants/service-constants";
import {topNavBarConstants} from "@/constants/top-nav-bar-constants";
import {getCsrfToken, signOut} from "next-auth/react";
import {Session} from "next-auth";
import {useEffect, useState} from "react";

export default function TopNavBar({session}: { session: Session }) {
    const [serviceLogoExists, setServiceLogoExists] = useState(false);

    useEffect(() => {
        const img = new Image();
        img.onload = () => setServiceLogoExists(true);
        img.onerror = () => setServiceLogoExists(false);
        img.src = service.nameImage.src;
    }, []);

    return (
        <TopNavigation
            identity={{
                href: "/sessions",
                logo: serviceLogoExists ? {
                    src: service.nameImage.src,
                    alt: service.nameImage.alt
                } : undefined
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
                            await signOut()
                            await fetch("/api/auth/postSignOut", {
                                method: "POST",
                            });
                            await getCsrfToken()
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
