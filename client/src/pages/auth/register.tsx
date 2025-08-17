import { Button, Divider, Form, Input, Row, Select, Steps, message, notification } from 'antd';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { callRegisterSendOtp, callRegisterVerify } from 'config/api';
import styles from 'styles/auth.module.scss';
import { IUser } from '@/types/backend';
const { Option } = Select;


const RegisterPage = () => {
    const navigate = useNavigate();
    const [isSubmit, setIsSubmit] = useState(false);
    const [step, setStep] = useState<number>(0);
    const [cached, setCached] = useState<any>({});

    const onSendOtp = async (values: any) => {
        const { name, email, password, confirmPassword, age, gender, address } = values;
        if (password !== confirmPassword) {
            notification.error({ message: 'Mật khẩu xác nhận không khớp' });
            return;
        }
        setIsSubmit(true);
        const res = await callRegisterSendOtp(name, email, password as string, confirmPassword as string, +age, gender, address);
        setIsSubmit(false);
        const isSuccess = res && String(res.statusCode || '').startsWith('2');
        if (isSuccess) {
            setCached({ name, email, password, confirmPassword, age, gender, address });
            message.success('Đã gửi OTP tới email, vui lòng kiểm tra.');
            setStep(1);
        } else {
            notification.error({
                message: "Có lỗi xảy ra",
                description: res.message && Array.isArray(res.message) ? res.message[0] : res.message,
                duration: 5
            })
        }
    };

    const onVerify = async (values: any) => {
        const { otp } = values;
        setIsSubmit(true);
        const res = await callRegisterVerify(cached.name, cached.email, cached.password, cached.confirmPassword, +cached.age, cached.gender, cached.address, otp);
        setIsSubmit(false);
        const ok = res && String(res.statusCode || '').startsWith('2');
        if (ok && res?.data?._id) {
            message.success('Đăng ký tài khoản thành công!');
            navigate('/login')
        } else {
            notification.error({
                message: "Có lỗi xảy ra",
                description: res.message && Array.isArray(res.message) ? res.message[0] : res.message,
                duration: 5
            })
        }
    }


    return (
        <div className={styles["register-page"]} >

            <main className={styles.main} >
                <div className={styles.container} >
                    <section className={styles.wrapper} >
                        <div className={styles.heading} >
                            <h2 className={`${styles.text} ${styles["text-large"]}`}> Đăng Ký Tài Khoản </h2>
                            < Divider />
                        </div>
                        <Steps current={step} items={[{ title: 'Nhập thông tin' }, { title: 'Nhập OTP' }]} style={{ marginBottom: 24 }} />
                        {step === 0 && (
                        < Form<IUser>
                            name="basic"
                            // style={{ maxWidth: 600, margin: '0 auto' }}
                            onFinish={onSendOtp}
                            autoComplete="off"
                        >
                            <Form.Item
                                labelCol={{ span: 24 }} //whole column
                                label="Họ tên"
                                name="name"
                                rules={[{ required: true, message: 'Họ tên không được để trống!' }]}
                            >
                                <Input />
                            </Form.Item>


                            <Form.Item
                                labelCol={{ span: 24 }
                                } //whole column
                                label="Email"
                                name="email"
                                rules={[{ required: true, message: 'Email không được để trống!' }]}
                            >
                                <Input type='email' />
                            </Form.Item>

                            <Form.Item
                                labelCol={{ span: 24 }} //whole column
                                label="Mật khẩu"
                                name="password"
                                rules={[{ required: true, message: 'Mật khẩu không được để trống!' }]}
                            >
                                <Input.Password />
                            </Form.Item>
                            <Form.Item
                                labelCol={{ span: 24 }} //whole column
                                label="Xác nhận mật khẩu"
                                name="confirmPassword"
                                rules={[{ required: true, message: 'Xác nhận mật khẩu không được để trống!' }]}
                            >
                                <Input.Password />
                            </Form.Item>
                            <Form.Item
                                labelCol={{ span: 24 }} //whole column
                                label="Tuổi"
                                name="age"
                                rules={[{ required: true, message: 'Tuổi không được để trống!' }]}
                            >
                                <Input type='number' />
                            </Form.Item>


                            <Form.Item
                                labelCol={{ span: 24 }} //whole column
                                name="gender"
                                label="Giới tính"
                                rules={[{ required: true, message: 'Giới tính không được để trống!' }]}
                            >
                                <Select
                                    // placeholder="Select a option and change input text above"
                                    // onChange={onGenderChange}
                                    allowClear
                                >
                                    <Option value="male">Nam</Option>
                                    <Option value="female">Nữ</Option>
                                    <Option value="other">Khác</Option>
                                </Select>
                            </Form.Item>


                            <Form.Item
                                labelCol={{ span: 24 }} //whole column
                                label="Địa chỉ"
                                name="address"
                                rules={[{ required: true, message: 'Địa chỉ không được để trống!' }]}
                            >
                                <Input />
                            </Form.Item>

                            < Form.Item
                            // wrapperCol={{ offset: 6, span: 16 }}
                            >
                                <Button type="primary" htmlType="submit" loading={isSubmit} >
                                    Gửi OTP
                                </Button>
                            </Form.Item>
                            <Divider> Or </Divider>
                            <p className="text text-normal" > Đã có tài khoản ?
                                <span>
                                    <Link to='/login' > Đăng Nhập </Link>
                                </span>
                            </p>
                        </Form>
                        )}
                        {step === 1 && (
                            <Form onFinish={onVerify}>
                                <Form.Item labelCol={{ span: 24 }} label="OTP" name="otp" rules={[{ required: true, message: 'OTP không được để trống!' }]}>
                                    <Input />
                                </Form.Item>
                                <Form.Item>
                                    <Button type="primary" htmlType="submit" loading={isSubmit}>Xác thực & Tạo tài khoản</Button>
                                </Form.Item>
                            </Form>
                        )}
                    </section>
                </div>
            </main>
        </div>
    )
}

export default RegisterPage;