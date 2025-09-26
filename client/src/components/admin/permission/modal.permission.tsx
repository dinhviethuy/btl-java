import { callCreatePermission, callUpdatePermission } from "@/config/api";
import { ALL_MODULES } from "@/config/permissions";
import { colorMethod } from "@/config/utils";
import { IPermission } from "@/types/backend";
import { ModalForm, ProFormSelect, ProFormText } from "@ant-design/pro-components";
import { Col, Form, Row, message, notification } from "antd";
import { useEffect } from "react";
import { isMobile } from 'react-device-detect';

interface IProps {
    openModal: boolean;
    setOpenModal: (v: boolean) => void;
    dataInit?: IPermission | null;
    setDataInit: (v: any) => void;
    reloadTable: () => void;
}



const ModalPermission = (props: IProps) => {
    const { openModal, setOpenModal, reloadTable, dataInit, setDataInit } = props;
    const [form] = Form.useForm();

    useEffect(() => {
        if (openModal) {
            if (dataInit?._id) {
                form.setFieldsValue(dataInit);
            } else {
                form.resetFields();
            }
        }
    }, [openModal, dataInit, form]);
    const submitPermission = async (valuesForm: any) => {
        const { name, apiPath, method, module } = valuesForm;
        if (dataInit?._id) {
            //update
            const permission = {
                name,
                apiPath, method, module
            }

            const res = await callUpdatePermission(permission, dataInit._id);
            if (res.data) {
                message.success("Cập nhật permission thành công");
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
            const permission = {
                name,
                apiPath, method, module
            }
            const res = await callCreatePermission(permission);
            if (res.data) {
                message.success("Thêm mới permission thành công");
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
        setOpenModal(false);
    }

    return (
        <>
            <ModalForm
                title={dataInit?._id ? "Cập nhật Permission" : "Tạo mới Permission"}
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
                onFinish={submitPermission}
            >
                <Row gutter={16}>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormText
                            label="Tên Permission"
                            name="name"
                            rules={[
                                { required: true, message: 'Vui lòng không bỏ trống' },
                            ]}
                            placeholder="Nhập name"
                        />
                    </Col>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormText
                            label="API Path"
                            name="apiPath"
                            rules={[
                                { required: true, message: 'Vui lòng không bỏ trống' },
                            ]}
                            placeholder="Nhập path"
                        />
                    </Col>

                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormSelect
                            name="method"
                            label="Method"
                            placeholder="Please select a method"
                            rules={[{ required: true, message: 'Vui lòng chọn method!' }]}
                            options={[
                                { label: <span style={{ color: colorMethod('GET'), fontWeight: 600 }}>GET</span>, value: 'GET' },
                                { label: <span style={{ color: colorMethod('POST'), fontWeight: 600 }}>POST</span>, value: 'POST' },
                                { label: <span style={{ color: colorMethod('PUT'), fontWeight: 600 }}>PUT</span>, value: 'PUT' },
                                { label: <span style={{ color: colorMethod('PATCH'), fontWeight: 600 }}>PATCH</span>, value: 'PATCH' },
                                { label: <span style={{ color: colorMethod('DELETE'), fontWeight: 600 }}>DELETE</span>, value: 'DELETE' },
                            ]}
                        />
                    </Col>
                    <Col lg={12} md={12} sm={24} xs={24}>
                        <ProFormSelect
                            name="module"
                            label="Thuộc Module"
                            valueEnum={ALL_MODULES}
                            placeholder="Please select a module"
                            rules={[{ required: true, message: 'Vui lòng chọn module!' }]}
                        />
                    </Col>

                </Row>
            </ModalForm>
        </>
    )
}

export default ModalPermission;
