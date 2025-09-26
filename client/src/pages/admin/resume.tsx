import ViewDetailResume from "@/components/admin/resume/view.resume";
import DataTable from "@/components/client/data-table";
import Access from "@/components/share/access";
import { callDeleteResume } from "@/config/api";
import { ALL_PERMISSIONS } from "@/config/permissions";
import { useAppDispatch, useAppSelector } from "@/redux/hooks";
import { fetchResume } from "@/redux/slice/resumeSlide";
import { IResume } from "@/types/backend";
import { DeleteOutlined, EyeOutlined, LinkOutlined } from "@ant-design/icons";
import { ActionType, ProColumns, ProFormSelect, ProFormText } from '@ant-design/pro-components';
import { Popconfirm, Space, message, notification } from "antd";
import dayjs from 'dayjs';
import queryString from 'query-string';
import { useEffect, useRef, useState } from 'react';

const ResumePage = () => {
    useEffect(() => { document.title = 'Hồ sơ ứng tuyển'; }, []);
    const tableRef = useRef<ActionType>();

    const isFetching = useAppSelector(state => state.resume.isFetching);
    const meta = useAppSelector(state => state.resume.meta);
    const resumes = useAppSelector(state => state.resume.result);
    const dispatch = useAppDispatch();

    const [dataInit, setDataInit] = useState<IResume | null>(null);
    const [openViewDetail, setOpenViewDetail] = useState<boolean>(false);

    const handleDeleteResume = async (_id: string | undefined) => {
        if (_id) {
            const res = await callDeleteResume(_id);
            if (res && res.data) {
                message.success('Xóa Resume thành công');
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

    const columns: ProColumns<IResume>[] = [
        {
            title: 'Id',
            dataIndex: '_id',
            width: 250,
            render: (text, record, index, action) => {
                return (
                    <a href="#" onClick={() => {
                        setOpenViewDetail(true);
                        setDataInit(record);
                    }}>
                        {record._id}
                    </a>
                )
            },
            hideInSearch: true,
        },
        {
            title: 'Trạng Thái',
            dataIndex: 'status',
            sorter: true,
            renderFormItem: (item, props, form) => (
                <ProFormSelect
                    showSearch
                    mode="multiple"
                    allowClear
                    placeholder="Trạng thái"
                    fieldProps={{
                        maxTagCount: 'responsive',
                        maxTagTextLength: 10,
                        maxTagPlaceholder: (omittedValues: any[]) => (
                            <span title={omittedValues.map((o: any) => String(o.label ?? o.value)).join(', ')}>+ {omittedValues?.length} ...</span>
                        )
                    }}
                    valueEnum={{
                        PENDING: 'PENDING',
                        REVIEWING: 'REVIEWING',
                        APPROVED: 'APPROVED',
                        REJECTED: 'REJECTED',
                    }}
                />
            ),
        },

        {
            title: 'Job',
            dataIndex: ["jobId", "name"],
            renderFormItem: () => (
                <ProFormText name="jobName" placeholder="Tìm theo tên job" />
            ),
        },
        {
            title: 'Company',
            dataIndex: ["companyId", "name"],
            renderFormItem: () => (
                <ProFormSelect
                    showSearch
                    debounceTime={300}
                    placeholder="Chọn công ty"
                    request={async ({ keyWords }) => {
                        const query = `current=1&pageSize=10&scope=admin&name=${keyWords ? encodeURIComponent('/' + keyWords + '/i') : ''}`;
                        const res = await import("@/config/api").then(m => m.callFetchCompany(query));
                        if (res && res.data) {
                            return res.data.result.map((c: any) => ({ label: c.name, value: c._id }));
                        }
                        return [];
                    }}
                    name="companyId"
                    fieldProps={{ labelInValue: true }}
                />
            ),
        },

        {
            title: 'CreatedAt',
            dataIndex: 'createdAt',
            width: 200,
            sorter: true,
            render: (text, record, index, action) => {
                return (
                    <>{dayjs(record.createdAt).format('DD-MM-YYYY HH:mm:ss')}</>
                )
            },
            hideInSearch: true,
        },
        {
            title: 'UpdatedAt',
            dataIndex: 'updatedAt',
            width: 200,
            sorter: true,
            render: (text, record, index, action) => {
                return (
                    <>{dayjs(record.updatedAt).format('DD-MM-YYYY HH:mm:ss')}</>
                )
            },
            hideInSearch: true,
        },
        {
            title: 'Actions',
            hideInSearch: true,
            width: 80,
            render: (_value, entity) => (
                <Space>
                    {entity.url && (
                        <a href={entity.url} target="_blank" rel="noreferrer" title="Mở CV">
                            <LinkOutlined style={{ fontSize: 18, color: '#1677ff' }} />
                        </a>
                    )}
                    <EyeOutlined
                        style={{ fontSize: 18, color: '#52c41a' }}
                        onClick={() => { setOpenViewDetail(true); setDataInit(entity); }}
                    />
                    <Access permission={ALL_PERMISSIONS.RESUMES.DELETE} hideChildren>
                        <Popconfirm
                            placement="leftTop"
                            title={"Xác nhận xóa resume"}
                            description={"Bạn có chắc chắn muốn xóa resume này ?"}
                            onConfirm={() => handleDeleteResume(entity._id)}
                            okText="Xác nhận"
                            cancelText="Hủy"
                        >
                            <span style={{ cursor: "pointer" }}>
                                <DeleteOutlined style={{ fontSize: 18, color: '#ff4d4f' }} />
                            </span>
                        </Popconfirm>
                    </Access>
                </Space>
            ),
        },
    ];

    const buildQuery = (params: any, sort: any, filter: any) => {
        const clone = { ...params };
        // if (clone.name) clone.name = `/${clone.name}/i`;
        // if (clone.salary) clone.salary = `/${clone.salary}/i`;
        if (clone?.status?.length) {
            clone.status = clone.status.join(",");
        }
        if (clone.companyId) {
            const cid = typeof clone.companyId === 'object' ? (clone.companyId.value || clone.companyId.key || clone.companyId._id) : clone.companyId;
            if (cid) clone.companyId = cid;
        }
        if (clone.jobId && typeof clone.jobId === 'object') {
            delete clone.jobId;
        }

        if (clone.jobName) clone.jobName = `/${String(clone.jobName).trim()}/i`;
        // companyName: dùng select nên không gửi companyName khi đã có companyId
        let temp = queryString.stringify(clone);

        let sortBy = "";
        if (sort && sort.status) {
            sortBy = sort.status === 'ascend' ? "sort=status" : "sort=-status";
        }

        if (sort && sort.createdAt) {
            sortBy = sort.createdAt === 'ascend' ? "sort=createdAt" : "sort=-createdAt";
        }
        if (sort && sort.updatedAt) {
            sortBy = sort.updatedAt === 'ascend' ? "sort=updatedAt" : "sort=-updatedAt";
        }

        //mặc định sort theo createdAt
        if (Object.keys(sortBy).length === 0) {
            temp = `${temp}&sort=-createdAt`;
        } else {
            temp = `${temp}&${sortBy}`;
        }

        temp += "&populate=companyId,jobId&fields=companyId._id, companyId.name, companyId.logo, jobId._id, jobId.name";
        return temp;
    }

    return (
        <div>
            <Access
                permission={ALL_PERMISSIONS.RESUMES.GET_PAGINATE}
            >
                <DataTable<IResume>
                    actionRef={tableRef}
                    headerTitle="Danh sách Resumes"
                    rowKey="_id"
                    loading={isFetching}
                    columns={columns}
                    dataSource={resumes}
                    request={async (params, sort, filter): Promise<any> => {
                        const query = buildQuery(params, sort, filter);
                        await dispatch(fetchResume({ query }));
                        return true as any;
                    }}
                    scroll={{ x: true }}
                    pagination={
                        {
                            current: meta.current,
                            pageSize: meta.pageSize,
                            showSizeChanger: true,
                            total: meta.total,
                            showTotal: (total, range) => { return (<div> {range[0]}-{range[1]} trên {total} rows</div>) }
                        }
                    }
                    rowSelection={false}
                    toolBarRender={(_action, _rows): any => {
                        return (
                            <></>
                        );
                    }}
                />
            </Access>
            <ViewDetailResume
                open={openViewDetail}
                onClose={setOpenViewDetail}
                dataInit={dataInit}
                setDataInit={setDataInit}
                reloadTable={reloadTable}
            />
        </div>
    )
}

export default ResumePage;