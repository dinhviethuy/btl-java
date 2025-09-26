import { callChangePassword, callFetchResumeByUser, callGetSubscriberSkills, callUpdateProfile, callUpdateSubscriber, callUploadSingleFile } from "@/config/api";
import { SKILLS_LIST } from "@/config/utils";
import { useAppSelector } from "@/redux/hooks";
import { setUserLoginInfo } from '@/redux/slice/accountSlide';
import { IResume } from "@/types/backend";
import { MonitorOutlined, UploadOutlined } from "@ant-design/icons";
import type { TabsProps } from 'antd';
import { Avatar, Button, Col, Form, Input, InputNumber, Modal, Row, Select, Table, Tabs, Upload, message, notification } from "antd";
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { useEffect, useState } from 'react';
import { isMobile } from "react-device-detect";
import { useDispatch } from 'react-redux';

interface IProps {
    open: boolean;
    onClose: (v: boolean) => void;
}

const UserResume = (props: any) => {
    const [listCV, setListCV] = useState<IResume[]>([]);
    const [isFetching, setIsFetching] = useState<boolean>(false);

    useEffect(() => {
        const init = async () => {
            setIsFetching(true);
            const res = await callFetchResumeByUser();
            if (res && res.data) {
                setListCV(res.data as IResume[])
            }
            setIsFetching(false);
        }
        init();
    }, [])

    const getCompanyName = (r: IResume) => typeof r.companyId === 'string' ? '' : (r.companyId?.name || '');
    const getJobName = (r: IResume) => typeof r.jobId === 'string' ? '' : (r.jobId?.name || '');
    const companyFilters = Array.from(new Set(listCV.map(getCompanyName).filter(Boolean)))
        .map(n => ({ text: n, value: n }));
    const jobFilters = Array.from(new Set(listCV.map(getJobName).filter(Boolean)))
        .map(n => ({ text: n, value: n }));
    const statusFilters = Array.from(new Set(listCV.map(r => r.status).filter(Boolean)))
        .map(s => ({ text: s, value: s }));

    const columns: ColumnsType<IResume> = [
        {
            title: 'STT',
            key: 'index',
            width: 50,
            align: "center",
            render: (text, record, index) => {
                return (
                    <>
                        {(index + 1)}
                    </>)
            }
        },
        {
            title: 'Công Ty',
            dataIndex: ["companyId", "name"],
            filters: companyFilters,
            onFilter: (value, record) => getCompanyName(record).toLowerCase().includes(String(value).toLowerCase()),
            sorter: (a, b) => getCompanyName(a).localeCompare(getCompanyName(b)),
        },
        {
            title: 'Vị trí',
            dataIndex: ["jobId", "name"],
            filters: jobFilters,
            onFilter: (value, record) => getJobName(record).toLowerCase().includes(String(value).toLowerCase()),
            sorter: (a, b) => getJobName(a).localeCompare(getJobName(b)),
        },
        {
            title: 'Trạng thái',
            dataIndex: "status",
            filters: statusFilters,
            onFilter: (value, record) => String(record.status) === String(value),
            sorter: (a, b) => String(a.status).localeCompare(String(b.status)),
        },
        {
            title: 'Ngày rải CV',
            dataIndex: "createdAt",
            render(value, record, index) {
                return (
                    <>{dayjs(record.createdAt).format('DD-MM-YYYY HH:mm:ss')}</>
                )
            },
            sorter: (a, b) => new Date(a.createdAt || '').getTime() - new Date(b.createdAt || '').getTime(),
        },
        {
            title: '',
            dataIndex: "",
            render(value, record, index) {
                return (
                    <a
                        href={`${record?.url}`}
                        target="_blank"
                    >Chi tiết</a>
                )
            },
        },
    ];

    return (
        <div>
            <Table<IResume>
                columns={columns}
                dataSource={listCV || []}
                loading={isFetching}
                rowKey={(r) => r._id as string}
                pagination={{ pageSize: 10, showSizeChanger: false }}
            />
        </div>
    )
}

const UserUpdateInfo = (props: any) => {
    const [form] = Form.useForm();
    const dispatch = useDispatch();
    const user = useAppSelector(state => state.account.user);
    const [uploading, setUploading] = useState<boolean>(false);
    const [preview, setPreview] = useState<string | undefined>(user?.avatar || undefined);
    const [avatarFile, setAvatarFile] = useState<File | undefined>(undefined);

    useEffect(() => {
        form.setFieldsValue({
            name: user?.name,
            age: user?.age,
            address: user?.address
        });
        // Reset avatar states khi user thay đổi
        setAvatarFile(undefined);
        setPreview(user?.avatar || undefined);
    }, [user])

    const onFinish = async (values: any) => {
        const { name, age, address } = values;
        const payload: { name?: string; age?: number; address?: string; avatar?: string | null } = {
            name,
            age: age !== undefined && age !== null ? Number(age) : undefined,
            address,
            avatar: user?.avatar || null
        };

        // Xử lý avatar: nếu có file mới thì upload, nếu không thì gửi avatar cũ
        if (avatarFile) {
            // Có file mới -> upload lấy URL mới
            try {
                setUploading(true);
                const up = await callUploadSingleFile(avatarFile, 'avatar');
                if (up?.data?.url) {
                    payload.avatar = up.data.url;
                }
            } finally {
                setUploading(false);
            }
        } else {
            // Không có file mới -> gửi avatar cũ để giữ nguyên
            payload.avatar = user?.avatar || null;
        }

        const res = await callUpdateProfile(payload);
        if (res && res.data) {
            message.success("Cập nhật thông tin thành công");
            dispatch(setUserLoginInfo(res.data));
            // Reset avatar file state sau khi update thành công
            setAvatarFile(undefined);
            // Cập nhật preview với avatar mới từ server
            setPreview(res.data.avatar || undefined);
        } else {
            notification.error({ message: 'Có lỗi xảy ra', description: res?.message });
        }
    }

    return (
        <Form form={form} onFinish={onFinish} layout="vertical">
            <Row gutter={[20, 20]}>
                <Col span={24}>
                    <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                        <Avatar size={64} src={preview || user?.avatar} style={{ border: "1px solid #000" }}>
                            {!(preview || user?.avatar) && (user?.name?.substring(0, 2)?.toUpperCase() || 'NA')}
                        </Avatar>

                        <Upload
                            showUploadList={false}
                            beforeUpload={() => false}
                            accept="image/*"
                            onChange={(info) => {
                                const f: any = info?.file;
                                const raw: File | undefined = f?.originFileObj || (f instanceof File ? f : undefined);
                                if (!raw) return;
                                const reader = new FileReader();
                                reader.onload = (e) => {
                                    const result = String(e.target?.result || '');
                                    setPreview(result);
                                    setAvatarFile(raw);
                                };
                                reader.readAsDataURL(raw);
                            }}
                        >
                            <Button icon={<UploadOutlined />}>Chọn ảnh</Button>
                        </Upload>
                        {preview && preview !== user?.avatar && (
                            <Button onClick={() => {
                                setPreview(user?.avatar || undefined);
                                setAvatarFile(undefined);
                            }}>Xóa ảnh đã chọn</Button>
                        )}
                        {(preview || user?.avatar) && (
                            <Button danger onClick={async () => {
                                setPreview(undefined);
                                setAvatarFile(undefined);
                                const res = await callUpdateProfile({ avatar: null });
                                if (res?.data) {
                                    dispatch(setUserLoginInfo(res.data));
                                    message.success('Đã gỡ avatar');
                                    // Reset preview về undefined sau khi gỡ thành công
                                    setPreview(undefined);
                                }
                            }}>Gỡ avatar</Button>
                        )}
                    </div>

                </Col>
                <Col span={24}>
                    <Form.Item label={"Tên hiển thị"} name={"name"} rules={[{ required: true, message: 'Tên không được để trống!' }]}>
                        <Input />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item label={"Tuổi"} name={"age"}>
                        <InputNumber style={{ width: '100%' }} />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item label={"Địa chỉ"} name={"address"}>
                        <Input />
                    </Form.Item>
                </Col>
                <Col span={24}>
                    <Button type="primary" onClick={() => form.submit()}>Cập nhật</Button>
                </Col>
            </Row>
        </Form>
    )
}

const JobByEmail = (props: any) => {
    const [form] = Form.useForm();
    const user = useAppSelector(state => state.account.user);

    useEffect(() => {
        const init = async () => {
            const res = await callGetSubscriberSkills();
            if (res && res.data) {
                form.setFieldValue("skills", res.data.skills);
            }
        }
        init();
    }, [])


    const cacelJobByEmail = async () => {
        const res = await callUpdateSubscriber({
            email: user.email,
            name: user.name,
            skills: []
        });
        if (res.data) {
            message.success("Hủy nhận job qua email thành công");
            form.resetFields()
        } else {
            notification.error({
                message: 'Có lỗi xảy ra',
                description: res.message
            });
        }
    }

    const onFinish = async (values: any) => {
        const { skills } = values;
        const res = await callUpdateSubscriber({
            email: user.email,
            name: user.name,
            skills: skills ? skills : []
        });
        if (res.data) {
            message.success("Cập nhật thông tin thành công");
        } else {
            notification.error({
                message: 'Có lỗi xảy ra',
                description: res.message
            });
        }

    }

    return (
        <>
            <Form
                onFinish={onFinish}
                form={form}
            >
                <Row gutter={[20, 20]}>
                    <Col span={24}>
                        <Form.Item
                            label={"Kỹ năng"}
                            name={"skills"}
                            rules={[{ required: true, message: 'Vui lòng chọn ít nhất 1 skill!' }]}

                        >
                            <Select
                                mode="multiple"
                                allowClear
                                showArrow={false}
                                style={{ width: '100%' }}
                                placeholder={
                                    <>
                                        <MonitorOutlined /> Tìm theo kỹ năng...
                                    </>
                                }
                                optionLabelProp="label"
                                options={SKILLS_LIST}
                            />
                        </Form.Item>
                    </Col>
                    <Col span={24}>
                        <Button onClick={() => form.submit()}
                            type="primary"
                        >Cập nhật</Button>
                        <Button
                            style={{ marginLeft: 10 }}
                            type="default"
                            onClick={() => form.resetFields()}>Bỏ chọn tất cả
                        </Button>
                        <Button
                            style={{ marginLeft: 10 }}
                            type="primary"
                            danger
                            onClick={() => cacelJobByEmail()}
                        >Hủy nhận job qua email
                        </Button>
                    </Col>
                </Row>
            </Form>
        </>
    )
}

const UserChangePassword = () => {
    const [form] = Form.useForm();
    const [isSubmit, setIsSubmit] = useState<boolean>(false);

    const onFinish = async (values: any) => {
        const { pass, newPass, confirmNewPass } = values;
        if (newPass !== confirmNewPass) {
            notification.error({ message: 'Mật khẩu xác nhận không khớp' });
            return;
        }
        setIsSubmit(true);
        const res = await callChangePassword(pass, newPass, confirmNewPass);
        setIsSubmit(false);
        if (res && res.data !== undefined) {
            message.success('Đổi mật khẩu thành công');
            form.resetFields();
        } else {
            notification.error({ message: 'Có lỗi xảy ra', description: res?.message });
        }
    }

    return (
        <Form form={form} onFinish={onFinish} layout="vertical">
            <Row gutter={[20, 20]}>
                <Col span={24}>
                    <Form.Item label={"Mật khẩu hiện tại"} name={"pass"} rules={[{ required: true, message: 'Không được để trống' }]}>
                        <Input.Password />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item label={"Mật khẩu mới"} name={"newPass"} rules={[{ required: true, message: 'Không được để trống' }, { min: 6, message: 'Tối thiểu 6 ký tự' }]}>
                        <Input.Password />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item label={"Xác nhận mật khẩu mới"} name={"confirmNewPass"} rules={[{ required: true, message: 'Không được để trống' }]}>
                        <Input.Password />
                    </Form.Item>
                </Col>
                <Col span={24}>
                    <Button type="primary" loading={isSubmit} onClick={() => form.submit()}>Đổi mật khẩu</Button>
                </Col>
            </Row>
        </Form>
    )
}

const ManageAccount = (props: IProps) => {
    const { open, onClose } = props;

    const onChange = (key: string) => {
        // console.log(key);
    };

    const items: TabsProps['items'] = [
        {
            key: 'user-resume',
            label: `Rải CV`,
            children: <UserResume />,
        },
        {
            key: 'email-by-skills',
            label: `Nhận Jobs qua Email`,
            children: <JobByEmail />,
        },
        {
            key: 'user-update-info',
            label: `Cập nhật thông tin`,
            children: <UserUpdateInfo />,
        },
        {
            key: 'user-password',
            label: `Thay đổi mật khẩu`,
            children: <UserChangePassword />,
        },
    ];


    return (
        <>
            <Modal
                title="Quản lý tài khoản"
                open={open}
                onCancel={() => onClose(false)}
                maskClosable={false}
                footer={null}
                destroyOnClose={true}
                width={isMobile ? "100%" : "1200px"}
            >

                <div style={{ minHeight: 400 }}>
                    <Tabs
                        defaultActiveKey="user-resume"
                        items={items}
                        onChange={onChange}
                    />
                </div>

            </Modal>
        </>
    )
}

export default ManageAccount;