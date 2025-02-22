import { CSSProperties } from "react";

export interface BrandedHorizontalSplitLayoutProps {
    /**
     * Override the style of the left panel.
     */
    leftPanelStyle?: CSSProperties | undefined;
    /**
     * This will be used as the background image of the left panel
     *
     * Anything css background-image accepts can go here
     * It can be url|none|initial|inherit;
     * The url can be a base 64 encoded string that represents an image;
     * The url can be a linear-gradient() expression;
     * The url can be a combination of two or more mentioned above;
     */
    backgroundImageSrc?: string;
    /**
     * This will be used as the brand logo shows in the middle of left panel
     *
     * Anything img src accepts can go here
     * It can be an absolute url;
     * It can be a relative url;
     * It can be a base 64 encoded string that represents an image;
     */
    brandLogoSrc: string;
    /**
     * For accessibility purpose, an alt text for the logo image is required
     */
    brandLogoAlt: string;
    /**
     * Set crossOrigin value for brand logo
     */
    brandLogoCrossOrign?: "anonymous" | "use-credentials" | "" | undefined;
    /**
     * This will be used as the brand description after brand logo
     *
     * If a string is passed in, we will render it with a default style in <p> tag
     * If a react component is passed in, we will just render the component which can
     * be highly customized
     */
    brandDescription: string | React.ReactNode;
    /**
     * An optional header that shows on the top of right panel
     */
    headerComponent?: React.ReactNode;
    /**
     * An optional footer that shows on the bottom of right panel
     */
    footerComponent?: React.ReactNode;
    /**
     * An optional header that shows on the top of left panel
     */
    brandHeaderComponent?: React.ReactNode;
    /**
     * An optional footer that shows on the top of left panel
     */
    brandFooterComponent?: React.ReactNode;
    /**
     * This component accept a children prop as the main content.
     * We show it in the middle of the right panel.
     */
}