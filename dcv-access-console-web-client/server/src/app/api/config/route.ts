import type { NextApiRequest } from 'next'

export type ResponseData = {
    sessionScreenshotMaxWidth: string | undefined,
    sessionScreenshotMaxHeight: string | undefined
}
/**
 * API endpoint to fetch screenshot resolution parameters
 * This endpoint was created to:
 * - Provide a way to access server-side environment variables from client-side code
 * - Enable dynamic configuration changes through environment variables
 */
export async function GET(
    req: NextApiRequest
):Promise<Response> {
    const responseData: ResponseData = {
        sessionScreenshotMaxWidth: process.env.SESSION_SCREENSHOT_MAX_WIDTH,
        sessionScreenshotMaxHeight: process.env.SESSION_SCREENSHOT_MAX_HEIGHT
    };
    return Response.json(responseData, {
        status: 200,
        headers: {
            'Content-Type': 'application/json'
        }
    })
}
export const dynamic = "force-dynamic";