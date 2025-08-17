import AiChatWidget from "@/components/client/modal/ai.chat";
import AiSuggestWidget from "@/components/client/modal/ai.suggest";
import NotAuthenticated from "@/components/share/protected-route.ts/not-authenticated";
import NotPermitted from "@/components/share/protected-route.ts/not-permitted";
import { useAppDispatch, useAppSelector } from "@/redux/hooks";
import { setRefreshTokenAction } from "@/redux/slice/accountSlide";
import { message } from "antd";
import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";

interface IProps {
    children: React.ReactNode
}

const LayoutApp = (props: IProps) => {
    const isRefreshToken = useAppSelector(state => state.account.isRefreshToken);
    const errorRefreshToken = useAppSelector(state => state.account.errorRefreshToken);
    const navigate = useNavigate();
    const location = useLocation();
    const dispatch = useAppDispatch();

    //handle refresh token error
    useEffect(() => {
        if (isRefreshToken === true) {
            localStorage.removeItem('access_token')
            message.error(errorRefreshToken);
            dispatch(setRefreshTokenAction({ status: false, message: "" }))
            navigate('/login');
        }
    }, [isRefreshToken]);

    const isAdminRoute = location.pathname.startsWith('/admin');
    const isAuthenticated = useAppSelector(state => state.account.isAuthenticated);
    const isLoading = useAppSelector(state => state.account.isLoading);
    const permissions = useAppSelector(state => state.account.user.permissions);

    if (isAdminRoute && !isAuthenticated && !isLoading) {
        return <NotAuthenticated />
    }
    if (isAdminRoute && isAuthenticated && !isLoading && (!permissions || permissions.length === 0)) {
        return <NotPermitted />
    }
    return (<>
        {props.children}
        {/* AI widgets */}
        <AiChatWidget />
        <AiSuggestWidget />
    </>)
}

export default LayoutApp;