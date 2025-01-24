import {act, render, screen} from "@testing-library/react";
import {
    ServerDetailTabsEmpty, ServerDetailTabsWithoutAWS,
    ServerDetailTabsWithServerWithAws
} from "@/components/servers/server-detail-tabs/ServerDetailTabs.stories";
import {ServerDetailTabs} from "@/components/servers/server-detail-tabs/ServerDetailTabs";
import {SERVER_DETAIL_TABS_CONSTANTS} from "@/constants/server-detail-tabs-constants";
import createWrapper from "@cloudscape-design/components/test-utils/dom";
import {SERVER_SPLIT_PANEL_DETAILS_CONSTANTS} from "@/constants/server-split-panel-constants";

describe('ServerDetailTabs', () => {
    it('Should render empty', () => {
        render(<ServerDetailTabs {...ServerDetailTabsEmpty.args}/>)
        expect(screen.getByText(SERVER_DETAIL_TABS_CONSTANTS.EMPTY_TEXT, {})).toBeVisible()
    })
    it('Should render with server', () => {
        let wrapper
        act(() => {
            const {container} = render(<ServerDetailTabs {...ServerDetailTabsWithServerWithAws.args}/>)
            wrapper = createWrapper(container)
        })
        expect(wrapper.findTabs().findTabLinks()[0].getElement()).toHaveTextContent(SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.HOST)
        expect(wrapper.findTabs().findTabLinks()[1].getElement()).toHaveTextContent(SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.AWS)
        expect(wrapper.findTabs().findTabLinks()[2].getElement()).toHaveTextContent(SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.DCV_SERVER)
        expect(wrapper.findTabs().findTabLinks()[3].getElement()).toHaveTextContent(SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.CPU)
        expect(wrapper.findTabs().findTabLinks()[4].getElement()).toHaveTextContent(SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.GPU)
        expect(wrapper.findTabs().findTabLinks()[5].getElement()).toHaveTextContent(SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.SERVER_ENDPOINTS)
        expect(wrapper.findTabs().findTabLinks()[6].getElement()).toHaveTextContent(SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.TAGS)
    })
    it('Should render with server without aws', () => {
        let wrapper
        act(() => {
            const {container} = render(<ServerDetailTabs {...ServerDetailTabsWithoutAWS.args}/>)
            wrapper = createWrapper(container)
        })
        expect(wrapper.findTabs().findTabLinks()[0].getElement()).toHaveTextContent(SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.HOST)
        expect(wrapper.findTabs().findTabLinks()[1].getElement()).toHaveTextContent(SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.DCV_SERVER)
        expect(wrapper.findTabs().findTabLinks()[2].getElement()).toHaveTextContent(SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.CPU)
        expect(wrapper.findTabs().findTabLinks()[3].getElement()).toHaveTextContent(SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.GPU)
        expect(wrapper.findTabs().findTabLinks()[4].getElement()).toHaveTextContent(SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.SERVER_ENDPOINTS)
        expect(wrapper.findTabs().findTabLinks()[5].getElement()).toHaveTextContent(SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.TAGS)
    })

})
