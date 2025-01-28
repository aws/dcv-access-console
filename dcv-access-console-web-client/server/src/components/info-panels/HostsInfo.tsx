// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import * as React from "react";
import {HelpPanel, Icon} from '@cloudscape-design/components';
import {DOC_CONSTANTS} from "@/constants/doc-constants";

export default function HostsInfo() {
    return (
        <HelpPanel
            header={<h2>Hosts</h2>}
            footer={
                <div>
                    <h3>Learn more <Icon name="external" /></h3>
                    <ul>
                        <li><a href={DOC_CONSTANTS.HOSTS}>Hosts</a></li>
                    </ul>
                </div>
            }
        >
            <div>
                <p>
                    You can view a list of host machines (either cloud or on-premises) you have installed Amazon DCV servers configured with Amazon DCV Session Manager.
                    <br/><br/>
                    Before your users can connect to a Amazon DCV session, you must have hosts available for users to create sessions on. You can't spin up hosts, install Amazon DCV servers on hosts, or configure them with the Amazon DCV Session Manager from the console.
                    <br/><br/>
                    You can view available hosts, and their detailed information. You can configure the visible fields in the top navigation bar by selecting the gear icon.
                </p>
            </div>
        </HelpPanel>
    )
}
