// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import BrandedHorizontalSplitLayout
    from "@/components/common/branded-horizontal-split-layout/BrandedHorizontalSplitLayout";
import {BrandedHorizontalSplitLayoutProps} from "@/components/interfaces/BrandedHorizontalSplitLayoutProps";
import {service} from "@/constants/service-constants";

export default {
    title: 'components/common/BrandedHorizontalSplitLayout',
    component: BrandedHorizontalSplitLayout
}

const Template = (args: BrandedHorizontalSplitLayoutProps) => <BrandedHorizontalSplitLayout{...args}>
    Testing
</BrandedHorizontalSplitLayout>
export const BrandedHorizontalSplitNormal = Template.bind({})
// @ts-ignore
BrandedHorizontalSplitNormal.args = {
    leftPanelStyle: { boxShadow: "0px 0px 14px rgba(0, 0, 0, 0.1)" },
    brandHeaderComponent: <div
        style={{
            minHeight: '2.2rem',
            margin: '2.4rem',
        }}
    ></div>,
    brandFooterComponent: <div
        style={{
            fontSize: '14px',
            lineHeight: '22px',
            fontWeight: 400,
            margin: '2.4rem',
            display: 'flex',
            justifyContent: 'center',
        }}
    >
    </div>,
    brandLogoSrc: service.dcvLogo.src,
    brandLogoAlt: service.dcvLogo.alt,
    brandDescription: (
        <p style={{ fontSize: "68px", lineHeight: "74.21px", color: "#FFFFFF", letterSpacing: "1.54px", font: "Amazon Ember"}}>
            {service.tagline}
        </p>
    ),
    // backgroundImageSrc: "url('/login-background.png')",
}



