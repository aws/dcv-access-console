// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {Header, HeaderProps} from "@cloudscape-design/components";

export type HeaderWithCounterProps = HeaderProps & {
    counter?: number,
    openEnd?: boolean,
    loading?: boolean
};

export default function HeaderWithCounter(
    {
        children,
        variant,
        headingTagOverride,
        description,
        actions,
        counter,
        info,
        openEnd,
        loading,
    }: HeaderWithCounterProps) {

    return <Header
            variant={variant}
            headingTagOverride={headingTagOverride}
            description={description}
            actions={actions}
            counter={(() => {
                if (!loading) {
                    return counter >= 0 ? "(" + counter + (openEnd ? "+" : "") + ")" : ""
                }
                return ""
            })()}
            info={info}
    >
        {children}
    </Header>
}
