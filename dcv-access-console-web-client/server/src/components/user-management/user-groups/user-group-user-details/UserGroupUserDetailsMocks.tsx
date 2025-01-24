import {User} from "@/generated-src/client";
import {getDescribeUsers200Response} from "@/generated-src/msw/mock";

// @ts-ignore
const duplicateUsers: User[] = getDescribeUsers200Response().Users
export const USERS: User[] = duplicateUsers.filter((v,i,a)=>a.findIndex(v2=>(v2.UserId ===v.UserId))===i)
