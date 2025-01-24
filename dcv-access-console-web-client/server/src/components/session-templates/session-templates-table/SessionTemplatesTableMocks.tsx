import {SessionTemplate} from "@/generated-src/client";
import {getDescribeSessionTemplates200Response} from "@/generated-src/msw/mock";

// @ts-ignore
const duplicateSessionTemplates: SessionTemplate[] = getDescribeSessionTemplates200Response().SessionTemplates
export const SESSION_TEMPLATES: SessionTemplate[] = duplicateSessionTemplates.filter((v,i,a)=>a.findIndex(v2=>(v2.Id ===v.Id))===i)
