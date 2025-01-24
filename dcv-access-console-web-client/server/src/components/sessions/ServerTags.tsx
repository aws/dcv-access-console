import * as React from "react";
import Table from "@cloudscape-design/components/table";
import Box from "@cloudscape-design/components/box";
import {KeyValuePair} from "@/generated-src/client";
import {SERVER_TAG_CONSTANTS} from "@/constants/server-tags-constants";
import {Container, Header} from "@cloudscape-design/components";

export default function ServerTags({tags}: { tags: KeyValuePair[] }) {
    return (
        <Container>
            <Header variant={"h3"}>
                {SERVER_TAG_CONSTANTS.TABLE_HEADER}
            </Header>
            <Table
                variant="embedded"
                columnDefinitions={[
                    {
                        id: SERVER_TAG_CONSTANTS.KEY_ID,
                        header: SERVER_TAG_CONSTANTS.KEY_HEADER,
                        cell: e => e.Key,
                        minWidth: 165,
                        isRowHeader: true
                    },
                    {
                        id: SERVER_TAG_CONSTANTS.VALUE_ID,
                        header: SERVER_TAG_CONSTANTS.VALUE_HEADER,
                        cell: e => e.Value,
                        minWidth: 165,
                    }
                ]}
                items={tags!}
                loadingText={SERVER_TAG_CONSTANTS.LOADING_TEXT}
                resizableColumns
                empty={
                    <Box textAlign="center" color="inherit">
                        <b>{SERVER_TAG_CONSTANTS.EMPTY_TEXT}</b>
                        <Box
                            padding={{bottom: "s"}}
                            variant="p"
                            color="inherit"
                        >
                        </Box>
                    </Box>
                }
            /></Container>
    );
}
