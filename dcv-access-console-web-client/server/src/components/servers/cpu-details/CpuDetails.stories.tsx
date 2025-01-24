import {SERVERS} from "@/components/servers/servers-table/ServersTableMocks";
import CpuDetails from "@/components/servers/cpu-details/CpuDetails";

export default {
    title: 'components/server/CpuDetails',
    component: CpuDetails,
}

const Template = args => <CpuDetails{...args}/>
export const CpuDetailsEmpty = Template.bind({})
CpuDetailsEmpty.args = {
}

export const CpuDetailsNormal = Template.bind({})
CpuDetailsNormal.args = {
    cpu: SERVERS[0].Host.CpuInfo,
    cpuLoad: SERVERS[0].Host.CpuLoadAverage
}
