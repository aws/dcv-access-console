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
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";
import {useEffect, useState} from "react";

export type DcvHorizontalSplitLayoutProps = {
    addUsernamePassword: boolean;
    onClick?: CancelableEventHandler<ButtonProps.ClickDetail>;
};

export default function DcvHorizontalSplitLayout({addUsernamePassword, onClick}: DcvHorizontalSplitLayoutProps) {
    const [loading, setLoading] = useState(true)
    const [serviceLogoExists, setServiceLogoExists] = useState(false);
    const [footerLogoExists, setFooterLogoExists] = useState(false);
    useEffect(() => {
        setLoading(false)
    })
    useEffect(() => {
        const img = new Image();
        img.onload = () => setServiceLogoExists(true);
        img.onerror = () => setServiceLogoExists(false);
        img.src = service.nameImage;
    }, []);
    useEffect(() => {
        const img = new Image();
        img.onload = () => setFooterLogoExists(true);
        img.onerror = () => setFooterLogoExists(false);
        img.src = service.footerLogo.src;
    }, []);
    if(loading) return <LoadingSkeleton/>
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
                    <p style={{
                        fontFamily: "Open Sans"
                    }}>{DCV_HORIZONTAL_SPLIT_LAYOUT_CONSTANTS.COPYRIGHT_MESSAGE}</p>
                </div>}
                brandLogoSrc={serviceLogoExists ? service.nameImage : undefined}
                brandLogoAlt={service.serviceName}
                title={<h1 style={{
                    fontSize: "45px",
                    color: "#FFFFFF",
                    fontWeight: 700,
                    fontFamily: "Open Sans",
                    marginTop: "-15px"
                }}>
                    {serviceLogoExists ? undefined : service.serviceName}
                </h1>}
                brandDescription={<p style={{
                    fontSize: "60px",
                    lineHeight: "76.26px",
                    color: "#FFFFFF",
                    letterSpacing: "5px",
                    fontWeight: 100,
                    fontFamily: "Open Sans"
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
                        logoSrc={footerLogoExists ? service.footerLogo.src : undefined}
                        logoAlt={footerLogoExists  ? service.footerLogo.alt : undefined}
                    />
                </div>}
            >
                <LoginForm addUsernamePassword={addUsernamePassword}/>
            </BrandedHorizontalSplitLayout>
        </div>);
}