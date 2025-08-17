import { useAppSelector } from "@/redux/hooks";
import { Navigate } from "react-router-dom";

const PublicOnlyRoute = (props: any) => {
  const isAuthenticated = useAppSelector(state => state.account.isAuthenticated);
  const isLoading = useAppSelector(state => state.account.isLoading);
  // Cho phép render trang login/register ngay cả khi đang loading tài khoản
  if (isLoading) return <>{props.children}</>;
  if (isAuthenticated) return <Navigate to="/" replace />;
  return <>{props.children}</>;
}

export default PublicOnlyRoute;


