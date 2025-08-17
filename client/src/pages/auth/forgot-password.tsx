import { callResetPasswordByOtp, callSendOtp } from '@/config/api';
import { Button, Divider, Form, Input, message, notification, Steps } from 'antd';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from 'styles/auth.module.scss';

const ForgotPasswordPage = () => {
  const [isSubmit, setIsSubmit] = useState(false);
  const [step, setStep] = useState<number>(0);
  const [email, setEmail] = useState<string>('');
  const navigate = useNavigate();

  const onSendOtp = async (values: any) => {
    const { email } = values;
    setIsSubmit(true);
    const res = await callSendOtp(email);
    setIsSubmit(false);
    const isSuccess = res && String(res.statusCode || '').startsWith('2');
    if (isSuccess) {
      setEmail(email);
      message.success('Đã gửi OTP tới email');
      setStep(1);
    } else {
      notification.error({
        message: 'Có lỗi xảy ra',
        description: res?.message,
      });
    }
  }

  const onResetPassword = async (values: any) => {
    const { otp, newPassword, confirmNewPassword } = values;
    if (newPassword !== confirmNewPassword) {
      notification.error({ message: 'Mật khẩu xác nhận không khớp' });
      return;
    }
    setIsSubmit(true);
    const res = await callResetPasswordByOtp(email, otp, newPassword);
    setIsSubmit(false);
    const isSuccess = res && String(res.statusCode || '').startsWith('2');
    if (isSuccess) {
      message.success('Đặt lại mật khẩu thành công');
      navigate('/login');
    } else {
      notification.error({
        message: 'Có lỗi xảy ra',
        description: res?.message,
      });
    }
  }

  return (
    <div className={styles["login-page"]}>
      <main className={styles.main}>
        <div className={styles.container}>
          <section className={styles.wrapper}>
            <div className={styles.heading}>
              <h2 className={`${styles.text} ${styles["text-large"]}`}>Quên mật khẩu</h2>
              <Divider />
            </div>
            <Steps
              current={step}
              items={[
                { title: 'Nhập email' },
                { title: 'Nhập OTP & mật khẩu mới' },
              ]}
              style={{ marginBottom: 24 }}
            />
            {step === 0 && (
              <Form onFinish={onSendOtp} autoComplete="off">
                <Form.Item labelCol={{ span: 24 }} label="Email" name="email" rules={[{ required: true, message: 'Email không được để trống!' }, { type: 'email', message: 'Email không hợp lệ' }]}>
                  <Input />
                </Form.Item>
                <Form.Item>
                  <Button type="primary" htmlType="submit" loading={isSubmit}>
                    Gửi OTP
                  </Button>
                </Form.Item>
                <p className="text text-normal">Đã nhớ mật khẩu?
                  <span>
                    <Link to='/login' > Đăng nhập </Link>
                  </span>
                </p>
              </Form>
            )}
            {step === 1 && (
              <Form onFinish={onResetPassword} autoComplete="off">
                <Form.Item labelCol={{ span: 24 }} label="OTP" name="otp" rules={[{ required: true, message: 'OTP không được để trống!' }]}>
                  <Input />
                </Form.Item>
                <Form.Item labelCol={{ span: 24 }} label="Mật khẩu mới" name="newPassword" rules={[{ required: true, message: 'Mật khẩu mới không được để trống!' }, { min: 6, message: 'Tối thiểu 6 ký tự' }]}>
                  <Input.Password />
                </Form.Item>
                <Form.Item labelCol={{ span: 24 }} label="Xác nhận mật khẩu mới" name="confirmNewPassword" rules={[{ required: true, message: 'Xác nhận mật khẩu không được để trống!' }]}>
                  <Input.Password />
                </Form.Item>
                <Form.Item>
                  <Button type="primary" htmlType="submit" loading={isSubmit}>
                    Đặt lại mật khẩu
                  </Button>
                </Form.Item>
              </Form>
            )}
          </section>
        </div>
      </main>
    </div>
  )
}

export default ForgotPasswordPage;


