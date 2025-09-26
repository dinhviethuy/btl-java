import ModalCompany from "@/components/admin/company/modal.company";
import DataTable from "@/components/client/data-table";
import Access from "@/components/share/access";
import { callDeleteCompany } from "@/config/api";
import { ALL_PERMISSIONS } from "@/config/permissions";
import { useAppDispatch, useAppSelector } from "@/redux/hooks";
import { fetchCompany } from "@/redux/slice/companySlide";
import { ICompany } from "@/types/backend";
import { DeleteOutlined, EditOutlined, EyeOutlined, PlusOutlined } from "@ant-design/icons";
import { ActionType, ProColumns } from '@ant-design/pro-components';
import { Button, Popconfirm, Space, message, notification } from "antd";
import dayjs from 'dayjs';
import queryString from 'query-string';
import { useEffect, useRef, useState } from 'react';
import { useNavigate } from "react-router-dom";

const CompanyPage = () => {
    useEffect(() => { document.title = 'Công ty'; }, []);
    const [openModal, setOpenModal] = useState<boolean>(false);
    const [isView, setIsView] = useState<boolean>(false);
    const [dataInit, setDataInit] = useState<ICompany | null>(null);

    const tableRef = useRef<ActionType>();

    const isFetching = useAppSelector(state => state.company.isFetching);
    const meta = useAppSelector(state => state.company.meta);
    const companies = useAppSelector(state => state.company.result);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    const handleDeleteCompany = async (_id: string | undefined) => {
        if (_id) {
            const res = await callDeleteCompany(_id);
            if (res && res.data) {
                message.success('Xóa Company thành công');
                reloadTable();
            } else {
                notification.error({
                    message: 'Có lỗi xảy ra',
                    description: res.message
                });
            }
        }
    }

    const reloadTable = () => {
        tableRef?.current?.reload();
    }

    const columns: ProColumns<ICompany>[] = [
        {
            title: 'STT',
            key: 'index',
            width: 50,
            align: "center",
            render: (text, record, index) => (
                <> {(index + 1) + (meta.current - 1) * (meta.pageSize)} </>
            ),
            hideInSearch: true,
        },
        {
            title: 'Id',
            dataIndex: '_id',
            width: 250,
            render: (_, record) => <span>{record._id}</span>,
            hideInSearch: true,
        },
        {
            title: 'Name',
            dataIndex: 'name',
            sorter: true,
        },
        {
            title: 'Address',
            dataIndex: 'address',
            sorter: true,
        },
        {
            title: 'CreatedAt',
            dataIndex: 'createdAt',
            width: 200,
            sorter: true,
            render: (_, record) => <>{dayjs(record.createdAt).format('DD-MM-YYYY HH:mm:ss')}</>,
            hideInSearch: true,
        },
        {
            title: 'UpdatedAt',
            dataIndex: 'updatedAt',
            width: 200,
            sorter: true,
            render: (_, record) => <>{dayjs(record.updatedAt).format('DD-MM-YYYY HH:mm:ss')}</>,
            hideInSearch: true,
        },
        {
            title: 'Actions',
            hideInSearch: true,
            width: 50,
            render: (_value, entity) => (
                <Space>
                    <Access permission={ALL_PERMISSIONS.COMPANIES.GET_PAGINATE} hideChildren>
                        <EyeOutlined
                            style={{ fontSize: 20, color: '#1677ff', margin: "0 10px" }}
                            onClick={() => {
                                setIsView(true);
                                setOpenModal(true);
                                setDataInit(entity);
                            }}
                        />
                    </Access>

                    <Access permission={ALL_PERMISSIONS.COMPANIES.UPDATE} hideChildren>
                        <EditOutlined
                            style={{ fontSize: 20, color: '#ffa500' }}
                            onClick={() => {
                                setIsView(false);
                                setOpenModal(true);
                                setDataInit(entity);
                            }}
                        />
                    </Access>

                    <Access permission={ALL_PERMISSIONS.COMPANIES.DELETE} hideChildren>
                        <Popconfirm
                            placement="leftTop"
                            title={"Xác nhận xóa company"}
                            description={"Bạn có chắc chắn muốn xóa company này ?"}
                            onConfirm={() => handleDeleteCompany(entity._id)}
                            okText="Xác nhận"
                            cancelText="Hủy"
                        >
                            <span style={{ cursor: "pointer", margin: "0 10px" }}>
                                <DeleteOutlined style={{ fontSize: 20, color: '#ff4d4f' }} />
                            </span>
                        </Popconfirm>
                    </Access>
                </Space>
            ),
        },
    ];

    const buildQuery = (params: any, sort: any, _filter: any) => {
        const clone = { ...params };
        if (clone.name) clone.name = `/${clone.name}/i`;
        if (clone.address) clone.address = `/${clone.address}/i`;

        let temp = queryString.stringify(clone);

        let sortBy = "";
        if (sort?.name) sortBy = sort.name === 'ascend' ? "sort=name" : "sort=-name";
        if (sort?.address) sortBy = sort.address === 'ascend' ? "sort=address" : "sort=-address";
        if (sort?.createdAt) sortBy = sort.createdAt === 'ascend' ? "sort=createdAt" : "sort=-createdAt";
        if (sort?.updatedAt) sortBy = sort.updatedAt === 'ascend' ? "sort=updatedAt" : "sort=-updatedAt";

        // mặc định sort theo updatedAt
        if (!sortBy) {
            temp = `${temp}&sort=-updatedAt`;
        } else {
            temp = `${temp}&${sortBy}`;
        }

        return temp;
    }

    return (
        <div>
            <Access permission={ALL_PERMISSIONS.COMPANIES.GET_PAGINATE}>
                <DataTable<ICompany>
                    actionRef={tableRef}
                    headerTitle="Danh sách công ty"
                    rowKey="_id"
                    loading={isFetching}
                    columns={columns}
                    dataSource={companies}
                    request={async (params, sort, filter): Promise<any> => {
                        const query = buildQuery(params, sort, filter);
                        // nếu DataTable của bạn đòi return, trả về shape tối thiểu
                        await dispatch(fetchCompany({ query }));
                        return {
                            data: companies,
                            success: true,
                            total: meta.total,
                        };
                    }}
                    scroll={{ x: true }}
                    pagination={{
                        current: meta.current,
                        pageSize: meta.pageSize,
                        showSizeChanger: true,
                        total: meta.total,
                        showTotal: (total, range) => (<div>{range[0]}-{range[1]} trên {total} rows</div>)
                    }}
                    rowSelection={false}
                    toolBarRender={(_action, _rows): any => (
                        <Access permission={ALL_PERMISSIONS.COMPANIES.CREATE} hideChildren>
                            <Button
                                icon={<PlusOutlined />}
                                type="primary"
                                onClick={() => {
                                    setIsView(false);
                                    setDataInit(null);
                                    setOpenModal(true);
                                }}
                            >
                                Thêm mới
                            </Button>
                        </Access>
                    )}
                />
            </Access>

            {/* Chỉ 1 ModalCompany duy nhất */}
            <ModalCompany
                openModal={openModal}
                setOpenModal={setOpenModal}
                reloadTable={reloadTable}
                dataInit={dataInit}
                setDataInit={setDataInit}
                isView={isView}
                setIsView={setIsView}
            />
        </div>
    )
}

export default CompanyPage;
