import {Container, Header, Table} from "@cloudscape-design/components";
import {Endpoint} from "@/generated-src/client";
import {SERVER_ENDPOINTS_CONSTANTS} from "@/constants/server-endpoints-constants";
import Box from "@cloudscape-design/components/box";
import * as React from "react";

export default function ServerEndpoints({endpoints}: { endpoints: Endpoint[] }) {
    return <Container>
        <Header variant={"h3"}>
            {SERVER_ENDPOINTS_CONSTANTS.SERVER_ENDPOINTS_HEADER}
        </Header>
        <Table
        variant="embedded"
        empty={
            <Box textAlign="center" color="inherit">
                <b>{SERVER_ENDPOINTS_CONSTANTS.EMPTY_TEXT}</b>
                <Box
                    padding={{bottom: "s"}}
                    variant="p"
                    color="inherit"
                >
                </Box>
            </Box>
        }
        items={endpoints!}
        columnDefinitions={[
        {
            id: SERVER_ENDPOINTS_CONSTANTS.IP_ID,
            header: SERVER_ENDPOINTS_CONSTANTS.IP_HEADER,
            cell: endpoint => endpoint.IpAddress,
            isRowHeader: true
        },
        {
            id: SERVER_ENDPOINTS_CONSTANTS.PROTOCOL_ID,
            header: SERVER_ENDPOINTS_CONSTANTS.PROTOCOL_HEADER,
            cell: endpoint => endpoint.Protocol,
            isRowHeader: true
        },
        {
            id: SERVER_ENDPOINTS_CONSTANTS.PORT_ID,
            header: SERVER_ENDPOINTS_CONSTANTS.PORT_HEADER,
            cell: endpoint => endpoint.Port,
            isRowHeader: true
        },
            {
                id: SERVER_ENDPOINTS_CONSTANTS.WEB_URL_PATH_HEADER_ID,
                header: SERVER_ENDPOINTS_CONSTANTS.WEB_URL_PATH_HEADER_HEADER,
                cell: endpoint => endpoint.WebUrlPath,
                isRowHeader: true
            }
        ]}>
    </Table>
    </Container>
}
