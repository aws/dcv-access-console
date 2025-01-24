import {render} from "@testing-library/react";
import React from "react";
import SessionsSplitPanel from "@/components/common/sessions-split-panel/SessionsSplitPanel";
import {AppLayout} from "@cloudscape-design/components";
import {
    SessionsSplitPanelSelected,
    SessionsSplitPanelUnselected
} from "@/components/common/sessions-split-panel/SessionsSplitPanel.stories";

describe('SessionsSplitPanel', () => {
    it.skip("Should render", () => {
        render(<AppLayout splitPanel={<SessionsSplitPanel{...SessionsSplitPanelUnselected.args}/>}/>)
        // expect(screen.getByText(sessionCardsConstants.noSessionsMessage));

    })

    it.skip("Should render when selected", () => {
        // render(<SessionsSplitPanel{...SessionsSplitPanelSelected.args}/>)
        // expect(screen.getByText(sessionCardsConstants.noSessionsMessage));
    })
})