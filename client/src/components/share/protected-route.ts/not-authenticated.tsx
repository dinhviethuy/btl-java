import { Button, Result, Space } from 'antd';
import { useNavigate } from 'react-router-dom';

const NotAuthenticated = () => {
  const navigate = useNavigate();
  return (
    <Result
      status="warning"
      title="Chưa đăng nhập"
      subTitle="Bạn cần đăng nhập để truy cập nội dung này."
      extra={
        <Space>
          <Button onClick={() => navigate('/')}>Về trang chủ</Button>
          <Button type="primary" onClick={() => navigate('/login')}>Đăng nhập</Button>
        </Space>
      }
    />
  )
};

export default NotAuthenticated;


