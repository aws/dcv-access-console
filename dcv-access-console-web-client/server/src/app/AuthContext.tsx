"use client";

import {SessionProvider} from "next-auth/react";
import {Session} from "next-auth";

export interface AuthContextProps {
    children: React.ReactNode
    session: Session
}

export default function AuthContext({session, children}: AuthContextProps) {
    return <SessionProvider session={session}>{children}</SessionProvider>;
}
