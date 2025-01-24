import "@cloudscape-design/global-styles/index.css"
import AuthContext from "@/app/AuthContext";
import {Session} from "next-auth";
import {FlashBarContextProvider} from "@/context-providers/FlashBarContext";
import {headers} from "next/headers";

export default function RootLayout({
  children,
  session
}: {
  children: React.ReactNode
  session: Session
}) {
    // Call this to force a server side render for each page to add nonce
    // There doesn't seem to be another way to make all pages render dynamically
    headers()
    
    return (
        <html lang="en">
        <body>
        <FlashBarContextProvider>
            <AuthContext session={session}>{children}</AuthContext>
        </FlashBarContextProvider>
        </body>
        </html>
    )
}
