'use client'
import React, { PropsWithChildren } from "react";
import {BrandedHorizontalSplitLayoutProps} from "@/components/interfaces/BrandedHorizontalSplitLayoutProps";
import './BrandedHorizontalSplitLayout.css'
import Box from "@cloudscape-design/components/box";

const DEFAULT_LEFT_PANEL_BOX_SHADOW = "0px 0px 14px rgba(0, 0, 0, 1)";

// @ts-ignore
const BrandedHorizontalSplitLayout: React.FC<
    PropsWithChildren<BrandedHorizontalSplitLayoutProps>
    > = ({
             leftPanelStyle,
             backgroundImageSrc,
             brandLogoSrc,
             brandLogoAlt,
             brandLogoCrossOrign,
             brandDescription,
             headerComponent,
             footerComponent,
             brandHeaderComponent,
             brandFooterComponent,
             children,
         }) => {
    return (
        <div
            className="branded-horizontal-split-layout-container"
            style={{ backgroundImage: backgroundImageSrc }}
        >
            <div
                className="branded-horizontal-split-layout-left-panel"
                style={{
                    boxShadow: DEFAULT_LEFT_PANEL_BOX_SHADOW,
                    ...(leftPanelStyle ?? {}),
                }}
            >
                <div className="branded-horizontal-split-layout-brand-header">
                    {brandHeaderComponent}
                </div>
                <div className="branded-horizontal-split-layout-brand-container">
                    <img
                        className="branded-horizontal-split-layout-brand-logo"
                        src={brandLogoSrc}
                        alt={brandLogoAlt}
                        crossOrigin={brandLogoCrossOrign}
                    />
                    <div className="branded-horizontal-split-layout-brand-description">
                        {React.isValidElement(brandDescription) ? (
                            brandDescription
                        ) : (
                            <Box>{brandDescription}</Box>
                        )}
                    </div>
                </div>
                <div className="branded-horizontal-split-layout-brand-footer">
                    {brandFooterComponent}
                </div>
            </div>
            <div className="branded-horizontal-split-layout-right-panel">
                <div className="branded-horizontal-split-layout-header">
                    {headerComponent}
                </div>
                <div className="branded-horizontal-split-layout-main-content">
                    {children}
                </div>
                <div className="branded-horizontal-split-layout-footer">
                    {footerComponent}
                </div>
            </div>
        </div>
    );
};

export default BrandedHorizontalSplitLayout;
