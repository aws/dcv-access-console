import {render, screen} from "@testing-library/react";
import ServerEndpoints from "@/components/servers/server-endpoints/ServerEndpoints";
import {
    EmptyServerEndpoints,
    ServerEndpointsNormal
} from "@/components/servers/server-endpoints/ServerEndpoints.stories";
import {SERVER_ENDPOINTS_CONSTANTS} from "@/constants/server-endpoints-constants";

describe('ServerEndpoints', () => {
    it('Should render when empty', () => {
        render(<ServerEndpoints {...EmptyServerEndpoints.args}/>)
        expect(screen.getByText(SERVER_ENDPOINTS_CONSTANTS.EMPTY_TEXT, {}));
    })
    it('Should render many', () => {
        render(<ServerEndpoints {...ServerEndpointsNormal.args}/>)
        ServerEndpointsNormal.args.endpoints.forEach(endpoint => {
            expect(screen.getByRole("row", {name: endpoint.IpAddress + " " + endpoint.Protocol + " " + endpoint.Port + " " + endpoint.WebUrlPath})).toBeVisible();
        })
    })

})
