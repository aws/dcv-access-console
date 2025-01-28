// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

'use client'
import BrandedHorizontalSplitLayout
    from "@/components/common/branded-horizontal-split-layout/BrandedHorizontalSplitLayout";
import {service} from "@/constants/service-constants";
import LoginForm from "@/components/common/login-form/LoginForm";
import {CancelableEventHandler} from "@cloudscape-design/components/internal/events";
import {ButtonProps} from "@cloudscape-design/components/button/interfaces";
import {DCV_HORIZONTAL_SPLIT_LAYOUT_CONSTANTS} from "@/constants/dcv-horizontal-split-layout-constants";
import TermsAndConditions
    from "@/components/common/dcv-horizontal-split-layout/terms-and-conditions/TermsAndConditions";
import {signIn} from "next-auth/react";
import {useSearchParams} from "next/navigation";
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";
import {useEffect, useState} from "react";
import Error from "@/components/common/error/Error";

export type DcvHorizontalSplitLayoutProps = {
    addUsernamePassword: boolean;
    onClick?: CancelableEventHandler<ButtonProps.ClickDetail>;
};

export default function DcvHorizontalSplitLayout({addUsernamePassword, onClick}: DcvHorizontalSplitLayoutProps) {
    const [loading, setLoading] = useState(true)
    useEffect(() => {
        setLoading(false)
    }, [])

    if(!addUsernamePassword) {
        const searchParams = useSearchParams()
        const callbackUrl = searchParams.get("callbackUrl") || process.env.NEXT_PUBLIC_DEFAULT_PATH
        if(searchParams.get("error") === "OAuthCallback") {
            return <Error title={"Error"} text={"Error contacting the Authentication Server or the Handler"} confirmPath={"/"}/>
        }
        signIn(process.env.NEXT_PUBLIC_SM_UI_AUTH_ID, {
            redirect: false,
            callbackUrl: callbackUrl,
        })
        return <LoadingSkeleton />
    }
    if(loading) return <LoadingSkeleton />
    return (
        <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: '100%'
        }}>
            <BrandedHorizontalSplitLayout
                leftPanelStyle={
                    {
                        boxShadow: "0px 0px 14px rgba(0, 0, 0, 0.1)"
                    }
                }
                brandHeaderComponent={<div
                    style={{
                        minHeight: '2.2rem',
                        margin: '2.4rem',
                    }}
                ></div>}
                brandFooterComponent={<div
                    style={{
                        fontSize: '14px',
                        lineHeight: '22px',
                        fontWeight: 400,
                        color: "white",
                        margin: '2.4rem',
                        display: 'flex',
                        justifyContent: 'center',
                    }}
                >
                    <p>{DCV_HORIZONTAL_SPLIT_LAYOUT_CONSTANTS.COPYRIGHT_MESSAGE}</p>
                </div>}
                brandLogoSrc={service.nameImage.src}
                brandLogoAlt={service.nameImage.alt}
                brandDescription={<p style={{
                    fontSize: "60px",
                    lineHeight: "76.26px",
                    color: "#FFFFFF",
                    letterSpacing: "5px",
                    fontWeight: 100,
                }}>
                    {service.tagline}
                </p>
                }
                footerComponent={<div
                    style={{
                        fontSize: '14px',
                        lineHeight: '22px',
                        fontWeight: 400,
                        color: "black",
                        margin: '2.4rem',
                        display: 'flex',
                        justifyContent: 'center',
                    }}
                >
                    <TermsAndConditions
                        logoSrc={service.footerLogo.src}
                        logoAlt={service.footerLogo.alt}
                    />
                </div>}
            >
                <LoginForm addUsernamePassword={addUsernamePassword}/>
            </BrandedHorizontalSplitLayout>
        </div>);
}
