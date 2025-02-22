'use client';
import Error from "@/components/common/error/Error";

export default function ErrorPage() {
    return (
        <Error title={"401 Unauthorized"} confirmPath={"/"}/>
    );

}
