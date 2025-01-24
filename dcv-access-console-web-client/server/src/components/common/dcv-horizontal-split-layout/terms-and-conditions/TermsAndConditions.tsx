import {Link, SpaceBetween} from "@cloudscape-design/components";
import {TERMS_AND_CONDITIONS_CONSTANTS} from "@/constants/terms-and-conditions-constants";
import './TermsAndConditions.css'
export type TermsAndConditionsProps = {
    logoSrc: string,
    logoAlt: string,
}

export default function TermsAndConditions({logoSrc, logoAlt}: TermsAndConditionsProps) {


    return (
            <SpaceBetween size={"m"} direction={"horizontal"}>
                <img
                    className={"footer-logo"}
                    src={logoSrc}
                    alt={logoAlt}
                    width={29}
                    height={17}
                />
                <div data-id={"privacy-div"} className={"make-shown"}>
                    <Link
                        data-id={"privacy-link"}
                        target={"_blank"}
                        href={"https://example.com"}
                    >{TERMS_AND_CONDITIONS_CONSTANTS.PRIVACY_NAME}
                    </Link>
                </div>
                <p data-id={"privacy-bar"} className={"make-shown"}>|</p>
                <Link
                    target={"_blank"}
                    href={TERMS_AND_CONDITIONS_CONSTANTS.TERMS_LINK}
                >{TERMS_AND_CONDITIONS_CONSTANTS.TERMS_NAME}
                </Link>
                <p data-id={"cookie-bar"} className={"make-shown"}>|</p>
                <div data-id={"cookie-div"} className={"make-shown"}>
                    <Link
                        data-id={"cookie-link"}
                        target={"_blank"}
                        href={"https://example.com"}
                    >{TERMS_AND_CONDITIONS_CONSTANTS.COOKIE_PREFERENCES_NAME}
                    </Link>
                </div>
            </SpaceBetween>
    )
}
