import {FormField, Header, Input, SpaceBetween} from "@cloudscape-design/components";
import Button from "@cloudscape-design/components/button";
import './LoginForm.css'
import {FormEventHandler, useEffect, useState} from "react";
import Form from "@cloudscape-design/components/form";
import {useSearchParams, useRouter} from "next/navigation";
import {signIn} from "next-auth/react";

export type LoginFormProps = {
    addUsernamePassword?: boolean;
};

export default function LoginForm({addUsernamePassword}: LoginFormProps) {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [csrf, setCsrf] = useState("");
    const searchParams = useSearchParams()
    const callbackUrl = searchParams.get("callbackUrl") || process.env.NEXT_PUBLIC_DEFAULT_PATH
    const router = useRouter()
    let errorMessage = ""
    let formContent
    let onAction: FormEventHandler<HTMLFormElement>

    if (searchParams.has("error")) {
        if (searchParams.get("error") === "OAuthSignin") {
            errorMessage = "Error contacting the authorization server."
        } else {
            errorMessage = "Invalid credentials"
        }
    }

    if (addUsernamePassword) {
        onAction = async () => {
            const csrfToken = document.cookie.replace(/(?:(?:^|.*;\s*)XSRF-TOKEN\s*\=\s*([^;]*).*$)|^.*$/, '$1');
            setCsrf(csrfToken)
            let bodyFormData = new FormData();
            bodyFormData.append("username", username);
            bodyFormData.append("password", password);
        }
        formContent = (<form
            action={"/login"}
            method={"POST"}
            onSubmit={(e) => {
                if (!addUsernamePassword) {
                    e.preventDefault()
                }
                onAction(e)
            }
            }>
            <Form
                variant="embedded"
            >
                <div className="login-form">
                    <SpaceBetween size={"l"}>
                        <Header
                            variant="h3"
                            description="Enter your company credentials."
                        >
                            <div className={"sign-in"}>
                                Log in
                            </div>
                        </Header>
                        <SpaceBetween size={"s"}>
                            <FormField label="Username"
                                       errorText={errorMessage}>
                                <div className="form-text-box">
                                    <Input
                                        controlId={"username"}
                                        name={"username"}
                                        value={username}
                                        placeholder="Username"
                                        onChange={event =>
                                            setUsername(event.detail.value)
                                        }
                                    />
                                </div>
                            </FormField>
                            <FormField label="Password"
                                       errorText={errorMessage}
                            >
                                <div className="form-text-box">
                                    <Input
                                        controlId={"password"}
                                        name={"password"}

                                        value={password}
                                        type="password"
                                        placeholder="Password"
                                        autoComplete={false}
                                        disableBrowserAutocorrect
                                        onChange={event =>
                                            setPassword(event.detail.value)
                                        }
                                    />
                                </div>
                            </FormField>
                            <FormField>
                                <input hidden={true} name={"_csrf"} value={csrf}/>
                            </FormField>
                            <FormField>
                                <Button fullWidth variant="primary">Log in</Button>
                            </FormField>
                        </SpaceBetween>
                    </SpaceBetween>
                </div>
            </Form>
        </form>)

    } else {
        formContent = (<div>Loading...</div>)
        if (typeof window !== "undefined") {
            signIn(process.env.NEXT_PUBLIC_SM_UI_AUTH_ID, {
                redirect: true,
                callbackUrl: callbackUrl,
            }).then(r => {
                console.log("Alright", r)

            }).catch(e => {
                console.log("Error")
            })
        }

    }
    return formContent
}
