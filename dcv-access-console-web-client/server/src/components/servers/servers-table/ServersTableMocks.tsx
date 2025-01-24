import {Server} from "@/generated-src/client";
import {getDescribeServers200Response} from "@/generated-src/msw/mock";

// @ts-ignore
const duplicateServers: Server[] = getDescribeServers200Response().Servers
export const SERVERS: Server[] = duplicateServers.filter((v,i,a)=>a.findIndex(v2=>(v2.Id ===v.Id))===i)
