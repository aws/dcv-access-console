import SessionsSplitPanel from "@/components/common/sessions-split-panel/SessionsSplitPanel";
import {getDescribeSessions200Response} from "@/generated-src/msw/mock";
import {AppLayout} from "@cloudscape-design/components";

export default {
    title: 'components/common/SessionsSplitPanel',
    component: SessionsSplitPanel
}

// Split Panel can only be used from within AppLayout
const Template = args => <AppLayout splitPanel={<SessionsSplitPanel{...args}/>}/>
export const SessionsSplitPanelUnselected = Template.bind({})
SessionsSplitPanelUnselected.args = {
    selectedSession: []
}

export const SessionsSplitPanelSelected = Template.bind({})
SessionsSplitPanelSelected.args = {
    selectedSession: getRandomSession()
}

function getRandomSession() {
    let sessions = getDescribeSessions200Response().Sessions
    return sessions[Math.floor(Math.random() * sessions.length)]
}
