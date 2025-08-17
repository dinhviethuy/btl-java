import { callCreateCompany, callUpdateCompany, callUploadSingleFile } from "@/config/api";
import { ICompany } from "@/types/backend";
import { CheckSquareOutlined, LoadingOutlined, PlusOutlined } from "@ant-design/icons";
import { FooterToolbar, ModalForm, ProCard, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { Button, Col, ConfigProvider, Form, Modal, Row, Upload, message, notification } from "antd";
import enUS from 'antd/lib/locale/en_US';
import { useEffect, useState } from "react";
import { isMobile } from 'react-device-detect';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import 'styles/reset.scss';
import { v4 as uuidv4 } from 'uuid';

interface IProps {
    openModal: boolean;
    setOpenModal: (v: boolean) => void;
    dataInit?: ICompany | null;
    setDataInit: (v: any) => void;
    reloadTable: () => void;
    isView?: boolean;
    setIsView?: (v: boolean) => void;
}

interface ICompanyForm {
    name: string;
    address: string;
}

interface ICompanyLogo {
    name: string;
    uid: string;
}

const ModalCompany = (props: IProps) => {
    const { openModal, setOpenModal, reloadTable, dataInit, setDataInit, isView = false, setIsView } = props;

    const [animation, setAnimation] = useState<string>('open');
    const [loadingUpload, setLoadingUpload] = useState<boolean>(false);
    const [dataLogo, setDataLogo] = useState<ICompanyLogo[]>([]);
    const [previewOpen, setPreviewOpen] = useState(false);
    const [previewImage, setPreviewImage] = useState('');
    const [previewTitle, setPreviewTitle] = useState('');

    const [value, setValue] = useState<string>("");
    const [form] = Form.useForm();

    useEffect(() => {
        if (dataInit?._id && dataInit?.description) {
            setValue(dataInit.description);
        } else {
            setValue("");
        }
        // defaultFileList đã hiển thị logo cũ, nhưng state riêng để validate khi create
        if (!dataInit?._id) setDataLogo([]);
    }, [dataInit]);

    const submitCompany = async (valuesForm: ICompanyForm) => {
        if (isView) return; // an toàn

        const { name, address } = valuesForm;

        // nếu là create mà chưa có logo ở state => báo lỗi
        if (!dataInit?._id && dataLogo.length === 0) {
            message.error('Vui lòng upload ảnh Logo');
            return;
        }

        if (dataInit?._id) {
            const logoName = dataLogo[0]?.name ?? dataInit.logo ?? "";
            const res = await callUpdateCompany(dataInit._id, name, address, value, logoName);
            if (res.data) {
                message.success("Cập nhật company thành công");
                handleReset();
                reloadTable();
            } else {
                notification.error({ message: 'Có lỗi xảy ra', description: res.message });
            }
        } else {
            const res = await callCreateCompany(name, address, value, dataLogo[0].name);
            if (res.data) {
                message.success("Thêm mới company thành công");
                handleReset();
                reloadTable();
            } else {
                notification.error({ message: 'Có lỗi xảy ra', description: res.message });
            }
        }
    }

    const handleReset = async () => {
        form.resetFields();
        setValue("");
        setDataInit(null);
        setDataLogo([]);
        setIsView?.(false);

        setAnimation('close');
        await new Promise(r => setTimeout(r, 400));
        setOpenModal(false);
        setAnimation('open');
    }

    const handleRemoveFile = (_file: any) => {
        setDataLogo([]);
    }

    const handlePreview = async (file: any) => {
        if (!file.originFileObj) {
            setPreviewImage(file.url);
            setPreviewOpen(true);
            setPreviewTitle(file.name || file.url.substring(file.url.lastIndexOf('/') + 1));
            return;
        }
        getBase64(file.originFileObj, (url: string) => {
            setPreviewImage(url);
            setPreviewOpen(true);
            setPreviewTitle(file.name || file.url.substring(file.url.lastIndexOf('/') + 1));
        });
    };

    const getBase64 = (img: any, callback: any) => {
        const reader = new FileReader();
        reader.addEventListener('load', () => callback(reader.result));
        reader.readAsDataURL(img);
    };

    const beforeUpload = (file: any) => {
        const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
        if (!isJpgOrPng) message.error('You can only upload JPG/PNG file!');
        const isLt2M = file.size / 1024 / 1024 < 2;
        if (!isLt2M) message.error('Image must smaller than 2MB!');
        return isJpgOrPng && isLt2M;
    };

    const handleChange = (info: any) => {
        if (info.file.status === 'uploading') setLoadingUpload(true);
        if (info.file.status === 'done') setLoadingUpload(false);
        if (info.file.status === 'error') {
            setLoadingUpload(false);
            message.error(info?.file?.error?.event?.message ?? "Đã có lỗi xảy ra khi upload file.");
        }
    };

    const handleUploadFileLogo = async ({ file, onSuccess, onError }: any) => {
        const res = await callUploadSingleFile(file, "company");
        if (res && res.data) {
            setDataLogo([{ name: res.data.url, uid: uuidv4() }]);
            onSuccess?.('ok');
        } else {
            setDataLogo([]);
            const error = new Error(res.message);
            onError?.({ event: error });
        }
    };

    // Ẩn toolbar khi xem chi tiết
    const quillModules = isView
        ? { toolbar: false }
        : {
            toolbar: [
                [{ header: [1, 2, 3, false] }],
                ['bold', 'italic', 'underline', 'strike'],
                [{ list: 'ordered' }, { list: 'bullet' }],
                ['link', 'image'],
                ['clean']
            ]
        };

    return (
        <>
            {openModal &&
                <>
                    <ModalForm
                        title={<>{dataInit?._id ? (isView ? "Xem chi tiết Company" : "Cập nhật Company") : "Tạo mới Company"}</>}
                        open={openModal}
                        modalProps={{
                            onCancel: () => { handleReset() },
                            afterClose: () => handleReset(),
                            destroyOnClose: true,
                            width: isMobile ? "100%" : 900,
                            footer: null,
                            keyboard: false,
                            maskClosable: false,
                            className: `modal-company ${animation}`,
                            rootClassName: `modal-company-root ${animation}`
                        }}
                        scrollToFirstError
                        preserve={false}
                        form={form}
                        onFinish={submitCompany}
                        initialValues={dataInit?._id ? dataInit : {}}
                        // Ẩn submit khi xem chi tiết, chỉ còn nút Đóng
                        submitter={
                            isView
                                ? {
                                    render: () => (
                                        <FooterToolbar>
                                            <Button type="primary" onClick={handleReset}>
                                                Đóng
                                            </Button>
                                        </FooterToolbar>
                                    )
                                }
                                : {
                                    render: (_: any, dom: any) => <FooterToolbar>{dom}</FooterToolbar>,
                                    submitButtonProps: { icon: <CheckSquareOutlined /> },
                                    searchConfig: {
                                        resetText: "Hủy",
                                        submitText: <>{dataInit?._id ? "Cập nhật" : "Tạo mới"}</>,
                                    }
                                }
                        }
                        // Khóa toàn bộ field pro-form khi xem
                        readonly={isView}
                    >
                        <Row gutter={16}>
                            <Col span={24}>
                                <ProFormText
                                    label="Tên công ty"
                                    name="name"
                                    rules={[{ required: !isView, message: 'Vui lòng không bỏ trống' }]}
                                    placeholder="Nhập tên công ty"
                                />
                            </Col>

                            <Col span={8}>
                                <Form.Item
                                    labelCol={{ span: 24 }}
                                    label="Ảnh Logo"
                                    name="logo"
                                    rules={
                                        isView
                                            ? []
                                            : [{
                                                required: true,
                                                message: 'Vui lòng không bỏ trống',
                                                validator: () => {
                                                    // nếu đang update và đã có logo cũ thì pass
                                                    if (dataInit?._id && dataInit?.logo) return Promise.resolve();
                                                    if (dataLogo.length > 0) return Promise.resolve();
                                                    return Promise.reject(false);
                                                }
                                            }]
                                    }
                                >
                                    <ConfigProvider locale={enUS}>
                                        <Upload
                                            name="logo"
                                            listType="picture-card"
                                            className="avatar-uploader"
                                            maxCount={1}
                                            multiple={false}
                                            customRequest={handleUploadFileLogo}
                                            beforeUpload={beforeUpload}
                                            onChange={handleChange}
                                            onRemove={handleRemoveFile}
                                            onPreview={handlePreview}
                                            defaultFileList={
                                                dataInit?._id
                                                    ? [{
                                                        uid: uuidv4(),
                                                        name: dataInit?.logo ?? "",
                                                        status: 'done',
                                                        url: `${dataInit?.logo}`,
                                                    }]
                                                    : []
                                            }
                                            disabled={isView}
                                            showUploadList={{ showRemoveIcon: !isView }}
                                        >
                                            {!isView && (
                                                <div>
                                                    {loadingUpload ? <LoadingOutlined /> : <PlusOutlined />}
                                                    <div style={{ marginTop: 8 }}>Upload</div>
                                                </div>
                                            )}
                                        </Upload>
                                    </ConfigProvider>
                                </Form.Item>
                            </Col>

                            <Col span={16}>
                                <ProFormTextArea
                                    label="Địa chỉ"
                                    name="address"
                                    rules={[{ required: !isView, message: 'Vui lòng không bỏ trống' }]}
                                    placeholder="Nhập địa chỉ công ty"
                                    fieldProps={{ autoSize: { minRows: 4 } }}
                                />
                            </Col>

                            <ProCard
                                title="Miêu tả"
                                headStyle={{ color: '#d81921' }}
                                style={{ marginBottom: 20 }}
                                headerBordered
                                size="small"
                                bordered
                            >
                                <Col span={24}>
                                    <ReactQuill
                                        theme="snow"
                                        value={value}
                                        onChange={setValue}
                                        readOnly={isView}
                                        modules={quillModules}
                                    />
                                </Col>
                            </ProCard>
                        </Row>
                    </ModalForm>

                    <Modal
                        open={previewOpen}
                        title={previewTitle}
                        footer={null}
                        onCancel={() => setPreviewOpen(false)}
                        style={{ zIndex: 1500 }}
                    >
                        <img alt="example" style={{ width: '100%' }} src={previewImage} />
                    </Modal>
                </>
            }
        </>
    )
}

export default ModalCompany;
