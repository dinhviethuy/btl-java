import { useAppSelector } from "@/redux/hooks";
import Loading from "../loading";
import NotAuthenticated from "./not-authenticated";

const RoleBaseRoute = (props: any) => {
    const user = useAppSelector(state => state.account.user);
    const userRole = user.role.name;

    return (<>{props.children}</>)
}

const ProtectedRoute = (props: any) => {
    const isAuthenticated = useAppSelector(state => state.account.isAuthenticated)
    const isLoading = useAppSelector(state => state.account.isLoading)

    return (
        <>
            {isLoading === true ?
                <Loading />
                :
                <>
                    {isAuthenticated === true ?
                        <>
                            <RoleBaseRoute>
                                {props.children}
                            </RoleBaseRoute>
                        </>
                        :
                        <NotAuthenticated />
                    }
                </>
            }
        </>
    )
}

export default ProtectedRoute;