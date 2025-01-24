import {render, screen} from "@testing-library/react";
import {SERVER_DETAILS_CONSTANTS} from "@/constants/server-details-constants";
import ServerDetails from "@/components/servers/server-details/ServerDetails";
import {
    DcvServerDetailsEmpty,
    DcvServerDetailsNormal, DcvServerDetailsServerFull, DcvServerDetailsServerUnavailable
} from "@/components/servers/dcv-server-details/DcvServerDetails.stories";
import DcvServerDetails from "@/components/servers/dcv-server-details/DcvServerDetails";

describe('ServerDetails', () => {
    it('Should render when empty', () => {
        render(<DcvServerDetails {...DcvServerDetailsEmpty.args}/>)
        expect(screen.getByText(SERVER_DETAILS_CONSTANTS.EMPTY_TEXT, {})).toBeVisible()
    })
    it('Should render with many', () => {
        render(<DcvServerDetails {...DcvServerDetailsNormal.args}/>)
        expect(screen.getByText(Buffer.from(DcvServerDetailsNormal.args.server.Id, 'base64').toString(), {})).toBeVisible();
    })

    it('Should render with server full', () => {
        render(<DcvServerDetails {...DcvServerDetailsServerFull.args}/>)
        expect(screen.getByText(Buffer.from(DcvServerDetailsNormal.args.server.Id, 'base64').toString(), {})).toBeVisible();
        expect(screen.getByText("Server full", {})).toBeVisible()
    })

    it('Should render with unavailable server', () => {
        render(<DcvServerDetails {...DcvServerDetailsServerUnavailable.args}/>)
        expect(screen.getByText(Buffer.from(DcvServerDetailsNormal.args.server.Id, 'base64').toString(), {})).toBeVisible();
        expect(screen.getByText("Server closed", {})).toBeVisible()

    })


})
