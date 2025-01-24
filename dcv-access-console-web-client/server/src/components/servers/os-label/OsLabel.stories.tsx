import OsLabel from "@/components/servers/os-label/OsLabel";
import {Os} from "@/generated-src/client";

export default {
    title: 'components/server/OsLabel',
    component: OsLabel,
}
const Template = args => <OsLabel{...args}/>

export const OsLabelLinux = Template.bind({})
OsLabelLinux.args = {
    os: {
        Family: 'linux'
    } as Os
}

export const OsLabelWindows = Template.bind({})
OsLabelWindows.args = {
    os: {
        Family: 'windows'
    } as Os
}

export const OsLabelUnknown = Template.bind({})
OsLabelUnknown.args = {
}
