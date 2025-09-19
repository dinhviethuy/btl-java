import { callCreateUser, callFetchCompany, callFetchRole, callUpdateUser } from "@/config/api";
import { IUser } from "@/types/backend";
import { ModalForm, ProForm, ProFormDigit, ProFormSelect, ProFormText } from "@ant-design/pro-components";
import { Col, Form, Row, message, notification } from "antd";
import { useEffect, useState } from "react";
import { isMobile } from 'react-device-detect';
import { DebounceSelect } from "./debouce.select";

interface IProps {
    openModal: boolean;
    setOpenModal: (v: boolean) => void;
    dataInit?: IUser | null;
    setDataInit: (v: any) => void;
    reloadTable: () => void;
}

export interface ICompanySelect {
    label: string;
    value: string;
    key?: string;
}

const ModalUser = (props: IProps) => {
    const { openModal, setOpenModal, reloadTable, dataInit, setDataInit } = props;
    const [company, setCompany] = useState<ICompanySelect | null>(null);
    const [role, setRole] = useState<ICompanySelect | null>(null);

    const [form] = Form.useForm();

    useEffect(() => {
        if (openModal) {
            if (dataInit?._id) {
                form.setFieldsValue({
                    ...dataInit,
                    role: dataInit.role
                        ? { label: (dataInit.role as any)?.name ?? dataInit.role, value: (dataInit.role as any)?._id ?? dataInit.role }
                        : null,
                    company: dataInit.company
                        ? { label: dataInit.company.name, value: dataInit.company._id }
                        : null,
                });
            } else {
                form.resetFields();
            }
        }
    }, [openModal, dataInit, form]);

    const submitUser = async (valuesForm: any) => {
        const { name, email, password, address, age, gender, role, company } = valuesForm;
        if (dataInit?._id) {
            //update
            const user: IUser = {
                name,
                email,
                password,
                age,
                gender,
                address,
                // gửi string id hoặc null để backend có thể detach khi null
                role: role ? (role as any).value ?? (role as any)._id : null,
                company: company ? {
                    _id: (company as any)?.value ?? (company as any)?._id,
                    name: (company as any)?.label ?? (company as any)?.name
                } : null
            }

            const res = await callUpdateUser(user, dataInit._id);
            if (res.data) {
                message.success("Cập nhật user thành công");
                handleReset();
                reloadTable();
            } else {
                notification.error({
                    message: 'Có lỗi xảy ra',
                    description: res.message
                });
            }
        } else {
            //create
            const user: IUser = {
                name,
                email,
                password,
                age,
                gender,
                address,
                role: role ? (role as any).value ?? (role as any)._id : null,
                company: company ? { _id: company.value, name: company.label } : null
            }
            const res = await callCreateUser(user);
            if (res.data) {
                message.success("Thêm mới user thành công");
                handleReset();
                reloadTable();
            } else {
                notification.error({
                    message: 'Có lỗi xảy ra',
                    description: res.message
                });
            }
        }
    }

    const handleReset = async () => {
        form.resetFields();
        setDataInit(null);
        setCompany(null);
        setRole(null);
        setOpenModal(false);
    }

    // Usage of DebounceSelect
    async function fetchCompanyList(name: string): Promise<ICompanySelect[]> {
        const search = name && name.trim().length > 0 ? `&name=/${name}/i` : "";
        const res = await callFetchCompany(`current=1&pageSize=100${search}`);
        if (res && res.data) {
            const list = res.data.result;
            const temp = list.map(item => {
                return {
                    label: item.name as string,
                    value: item._id as string
                }
            })
            return temp;
        } else return [];
    }

    async function fetchRoleList(name: string): Promise<ICompanySelect[]> {
        const search = name && name.trim().length > 0 ? `&name=/${name}/i` : "";
        const res = await callFetchRole(`current=1&pageSize=100${search}`);
        if (res && res.data) {
            const list = res.data.result;
            const temp = list.map(item => {
                return {
                    label: item.name as string,
                    value: item._id as string
                }
            })
            return temp;
        } else return [];
    }

    return (
        <>
            <ModalForm
                title={dataInit?._id ? "Cập nhật User" : "Tạo mới User"}
                open={openModal}
                modalProps={{
                    onCancel: handleReset,
                    afterClose: handleReset,
                    destroyOnClose: true,
                    width: isMobile ? "100%" : 900,
                    keyboard: false,
                    maskClosable: false,
                    okText: dataInit?._id ? "Cập nhật" : "Tạo mới",
                    cancelText: "Hủy"
                }}
                form={form}
                onFinish={submitUser}
            >
                <Row gutter={16}>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormText
                            label="Email"
                            name="email"
                            rules={[
                                { required: true, message: 'Vui lòng không bỏ trống' },
                                { type: 'email', message: 'Vui lòng nhập email hợp lệ' }
                            ]}
                            placeholder="Nhập email"
                        />
                    </Col>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormText.Password
                            disabled={dataInit?._id ? true : false}
                            label="Password"
                            name="password"
                            rules={[{ required: dataInit?._id ? false : true, message: 'Vui lòng không bỏ trống' }]}
                            placeholder="Nhập password"
                        />
                    </Col>
                    <Col lg={6} md={6} sm={24} xs={24}>
                        <ProFormText
                            label="Tên hiển thị"
                            name="name"
                            rules={[{ required: true, message: 'Vui lòng không bỏ trống' }]}
                            placeholder="Nhập tên hiển thị"
                        />
                    </Col>
                    <Col lg={6} md={6} sm={24} xs={24}>
                        <ProFormDigit
                            label="Tuổi"
                            name="age"
                            rules={[{ required: true, message: 'Vui lòng không bỏ trống' }]}
                            placeholder="Nhập nhập tuổi"
                        />
                    </Col>
                    <Col lg={6} md={6} sm={24} xs={24}>
                        <ProFormSelect
                            name="gender"
                            label="Giới Tính"
                            valueEnum={{
                                MALE: 'Nam',
                                FEMALE: 'Nữ',
                                OTHER: 'Khác',
                            }}
                            placeholder="Please select a gender"
                            rules={[{ required: true, message: 'Vui lòng chọn giới tính!' }]}
                        />
                    </Col>
                    <Col lg={6} md={6} sm={24} xs={24}>
                        <ProForm.Item
                            name="role"
                            label="Vai trò"

                        >
                            <DebounceSelect
                                allowClear
                                showSearch
                                defaultValue={role ?? undefined}
                                value={role ?? undefined}
                                placeholder="Chọn vai trò"
                                fetchOptions={fetchRoleList}
                                onChange={(newValue: any) => {
                                    // allowClear => newValue can be undefined/null
                                    if (!newValue || (Array.isArray(newValue) && newValue.length === 0)) {
                                        setRole(null);
                                    } else {
                                        setRole(Array.isArray(newValue) ? (newValue[0] as ICompanySelect) : (newValue as ICompanySelect));
                                    }
                                }}
                                style={{ width: '100%' }}
                            />
                        </ProForm.Item>

                    </Col>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProForm.Item
                            name="company"
                            label="Thuộc Công Ty"
                            rules={[]}
                        >
                            <DebounceSelect
                                allowClear
                                showSearch
                                defaultValue={company ?? undefined}
                                value={company ?? undefined}
                                placeholder="Chọn công ty"
                                fetchOptions={fetchCompanyList}
                                onChange={(newValue: any) => {
                                    setCompany(Array.isArray(newValue) ? (newValue[0] as ICompanySelect) : (newValue as ICompanySelect));
                                }}
                                style={{ width: '100%' }}
                            />
                        </ProForm.Item>

                    </Col>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormText
                            label="Địa chỉ"
                            name="address"
                            rules={[{ required: true, message: 'Vui lòng không bỏ trống' }]}
                            placeholder="Nhập địa chỉ"
                        />
                    </Col>
                </Row>
            </ModalForm>
        </>
    )
}

export default ModalUser;
