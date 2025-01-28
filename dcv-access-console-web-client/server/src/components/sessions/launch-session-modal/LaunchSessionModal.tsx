// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import * as React from "react";
import Modal from "@cloudscape-design/components/modal";
import Box from "@cloudscape-design/components/box";
import SpaceBetween from "@cloudscape-design/components/space-between";
import Button from "@cloudscape-design/components/button";
import {Checkbox, Grid, Link} from "@cloudscape-design/components";
import {COOKIE_CONSTANTS} from "@/constants/cookie-constants";

export type LaunchSessionProps = {
    sessionName?: string,
    clientName?: string,
    webclientUrl?: string
}

export default ({visible, setVisible, launchingSessionProps}: {
    visible: boolean,
    setVisible: (visible: boolean) => void,
    launchingSessionProps: LaunchSessionProps | undefined}) => {

    let [checked, setChecked] = React.useState(false)
    return (
        <Modal
            visible={visible}
            onDismiss={() => setVisible(false)}
            footer={
                <Grid gridDefinition={[ { colspan:{ "default":6, xxs:6 } },
                                        { colspan:{ "default":6, xxs:6 } }
                ]}>
                    <Checkbox checked={checked}
                              onChange={({ detail }) => {

                                  if (typeof window !== undefined) {
                                      localStorage.setItem(COOKIE_CONSTANTS.DONT_ASK_FOR_INSTALLATION_COOKIE_NAME, String(detail.checked));
                                  }
                                  setChecked(detail.checked)
                              }}>
                        Do not ask again
                    </Checkbox>
                    <Box float={"right"}>
                        <Button
                            variant="primary"
                            ariaLabel="Download the DCV client (opens new tab)"
                            href="https://download.nice-dcv.com/latest.html"
                            target="_blank"
                        >
                            Download Amazon DCV client
                        </Button>
                    </Box>
                </Grid>
            }
            header={`Connecting to \"${launchingSessionProps?.sessionName}\" session`}
        >
            <SpaceBetween size="s" direction={"vertical"}>
                <Box variant={"p"}>Click “<b>Open DCV Viewer</b>” to start the desktop client.</Box>
                <Box variant={"p"}>If you do not see a dialogue, please download the desktop client and then try to connect to your session again.</Box>
                <Box variant={"p"}>Alternatively, you can connect to your Amazon DCV session with your <Link external href={launchingSessionProps?.webclientUrl || "#"}>web browser</Link></Box>
            </SpaceBetween>
        </Modal>
    );
}
