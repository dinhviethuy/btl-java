import JobCard from "@/components/client/card/job.card";
import { callFetchCompanyById } from "@/config/api";
import { ICompany } from "@/types/backend";
import { EnvironmentOutlined } from "@ant-design/icons";
import { Col, Divider, Row, Skeleton } from "antd";
import parse from 'html-react-parser';
import { useEffect, useState } from 'react';
import { useLocation } from "react-router-dom";
import styles from 'styles/client.module.scss';


const ClientCompanyDetailPage = (props: any) => {
    const [companyDetail, setCompanyDetail] = useState<ICompany | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(false);

    let location = useLocation();
    let params = new URLSearchParams(location.search);
    const id = params?.get("id"); // job id

    useEffect(() => {
        const init = async () => {
            if (id) {
                setIsLoading(true)
                const res = await callFetchCompanyById(id);
                if (res?.data) {
                    setCompanyDetail(res.data)
                }
                setIsLoading(false)
            }
        }
        init();
    }, [id]);

    return (
        <div className={`${styles["container"]} ${styles["detail-job-section"]}`}>
            {isLoading ?
                <Skeleton />
                :
                <Row gutter={[44, 20]}>
                    {companyDetail && companyDetail._id &&
                        <>
                            <Col span={24} md={16}>
                                <div className={styles["header"]}>
                                    {companyDetail.name}
                                </div>

                                <div className={styles["location"]}>
                                    <EnvironmentOutlined style={{ color: '#58aaab' }} />&nbsp;{(companyDetail?.address)}
                                </div>

                                <Divider />
                                {parse(companyDetail?.description ?? "")}
                                <Divider />
                                {/* Nội dung giới thiệu công ty giữ bên trái */}
                            </Col>

                            <Col span={24} md={8}>
                                <div className={styles["company"]}>
                                    <div>
                                        <img
                                            alt="example"
                                            src={`${companyDetail?.logo}`}
                                            style={{ width: 200, height: 200, objectFit: 'contain' }}
                                        />
                                    </div>
                                    <div>
                                        {companyDetail?.name}
                                    </div>
                                </div>
                                {/* Danh sách Job của công ty đặt dưới logo */}
                                <div style={{ width: '100%', marginTop: 24 }}>
                                    <h2 style={{ fontSize: 18, marginBottom: 12 }}>Công việc đang tuyển</h2>
                                    <JobCard
                                        showPagination={false}
                                        filter={`companyId=${companyDetail._id}&scope=public`}
                                        hideHeader
                                        listDirection='vertical'
                                        defaultPageSize={4}
                                    />
                                </div>
                            </Col>
                        </>
                    }
                </Row>
            }
        </div>
    )
}
export default ClientCompanyDetailPage;