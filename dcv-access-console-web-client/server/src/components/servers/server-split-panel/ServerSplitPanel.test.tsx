import {act, render, screen} from "@testing-library/react";
import {SERVER_DETAILS_CONSTANTS} from "@/constants/server-details-constants";
import {
    ServerSplitPanelEmpty,
    ServerSplitPanelNormal, ServerSplitPanelWithoutAWS
} from "@/components/servers/server-split-panel/ServerSplitPanel.stories";
import ServerSplitPanel from "@/components/servers/server-split-panel/ServerSplitPanel";
import createWrapper from "@cloudscape-design/components/test-utils/dom";
import {SERVER_SPLIT_PANEL_DETAILS_CONSTANTS} from "@/constants/server-split-panel-constants";

describe('ServerSplitPanel', () => {
    it('Should render when empty', () => {
        render(<ServerSplitPanelEmpty {...ServerSplitPanelEmpty.args}/>)
        expect(screen.getByRole("heading", {name: SERVER_DETAILS_CONSTANTS.EMPTY_TEXT})).toBeVisible()
    })
    it('Should render with server', () => {
        let wrapper
        act(() => {
            const {container} = render(<ServerSplitPanelNormal {...ServerSplitPanelNormal.args}/>)
            wrapper = createWrapper(container)
        })
        expect(wrapper.findSplitPanel().findHeader().getElement()).toHaveTextContent(ServerSplitPanelNormal.args.server.Hostname)
    })
})
