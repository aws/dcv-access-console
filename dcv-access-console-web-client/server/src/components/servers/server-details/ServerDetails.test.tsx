import {act, render} from "@testing-library/react";
import ServerDetails from "@/components/servers/server-details/ServerDetails";
import {
    ServerDetailsEmpty, ServerDetailsError,
    ServerDetailsLoading,
    ServerDetailsWithServer
} from "@/components/servers/server-details/ServerDetails.stories";
import createWrapper from "@cloudscape-design/components/test-utils/dom";
import {SERVER_DETAILS_CONSTANTS} from "@/constants/server-details-constants";
import {SERVER_OVERVIEW_CONSTANTS} from "@/constants/server-overview-contants";

jest.mock('next/navigation', () => ({
    useRouter: jest.fn(),
    usePathname() {
        return "/home/admin/servers";
    },

}));

describe('ServerDetails', () => {
    it.skip('Should render empty', () => {
        let wrapper
        act(() => {
            const {container} = render(<ServerDetails {...ServerDetailsEmpty.args}/>)
            wrapper = createWrapper(container)
        })
        expect(wrapper.findHeader().getElement()).toHaveTextContent(SERVER_DETAILS_CONSTANTS.UNKNOWN)
    })
    it.skip('Should render with server', () => {
        let wrapper
        act(() => {
            const {container} = render(<ServerDetails {...ServerDetailsWithServer.args}/>)
            wrapper = createWrapper(container)
        })
        expect(wrapper.findHeader().getElement()).toHaveTextContent(ServerDetailsWithServer.args.server.Hostname)
        expect(wrapper.findContainer().findHeader().getElement()).toHaveTextContent(SERVER_OVERVIEW_CONSTANTS.HEADER)
        expect(wrapper.findTabs()).not.toBeNull()

    })

    it.skip('Should render with loading', () => {
        let wrapper
        act(() => {
            const {container} = render(<ServerDetails {...ServerDetailsLoading.args}/>)
            wrapper = createWrapper(container)
        })
        expect(wrapper.findStatusIndicator().getElement()).toHaveTextContent(SERVER_DETAILS_CONSTANTS.LOADING_TEXT)

    })
    it.skip('Should render with error', () => {
        let wrapper
        act(() => {
            const {container} = render(<ServerDetails {...ServerDetailsError.args}/>)
            wrapper = createWrapper(container)
        })
        expect(wrapper.findStatusIndicator().getElement()).toHaveTextContent(SERVER_DETAILS_CONSTANTS.ERROR_TEXT)

    })

})
