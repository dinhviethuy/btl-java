import JobCard from "@/components/client/card/job.card";
import ApplyModal from "@/components/client/modal/apply.modal";
import { callFetchJobById } from "@/config/api";
import { convertSlug, getLocationName } from "@/config/utils";
import { IJob } from "@/types/backend";
import { DollarOutlined, EnvironmentOutlined, HistoryOutlined } from "@ant-design/icons";
import { Col, Divider, Row, Skeleton, Tag } from "antd";
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import parse from 'html-react-parser';
import { useEffect, useState } from 'react';
import { Link, useLocation } from "react-router-dom";
import styles from 'styles/client.module.scss';
dayjs.extend(relativeTime)


const ClientJobDetailPage = (props: any) => {
    const [jobDetail, setJobDetail] = useState<IJob | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(false);

    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

    let location = useLocation();
    let params = new URLSearchParams(location.search);
    const id = params?.get("id"); // job id

    useEffect(() => {
        const init = async () => {
            if (id) {
                setIsLoading(true)
                const res = await callFetchJobById(id);
                if (res?.data) {
                    setJobDetail(res.data)
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
                <Row gutter={[20, 20]}>
                    {jobDetail && jobDetail._id &&
                        <>
                            <Col span={24} md={16}>
                                <div className={styles["header"]}>
                                    {jobDetail.name}
                                </div>
                                <div>
                                    <button
                                        onClick={() => setIsModalOpen(true)}
                                        className={styles["btn-apply"]}
                                    >Apply Now</button>
                                </div>
                                <Divider />
                                <div className={styles["skills"]}>
                                    {jobDetail?.skills?.map((item, index) => {
                                        return (
                                            <Link to={`/job?skills=${encodeURIComponent(item.toLowerCase())}`}>
                                                <Tag key={`${index}-key`} color="gold" >
                                                    {item}
                                                </Tag>
                                            </Link>
                                        )
                                    })}
                                </div>
                                <div className={styles["salary"]}>
                                    <DollarOutlined />
                                    <span>&nbsp;{(jobDetail.salary + "")?.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} đ</span>
                                </div>
                                <div className={styles["location"]}>
                                    <EnvironmentOutlined style={{ color: '#58aaab' }} />&nbsp;{getLocationName(jobDetail.location)}
                                </div>
                                <div>
                                    <HistoryOutlined /> {dayjs(jobDetail.updatedAt).fromNow()}
                                </div>
                                <Divider />
                                {parse(jobDetail.description)}
                            </Col>

                            <Col span={24} md={8}>
                                <div className={styles["company"]}>
                                    <div>
                                        <Link to={`/company/${jobDetail.company?.name ? convertSlug(jobDetail.company.name) : ''}?id=${jobDetail.company?._id}`}>
                                            <img
                                                alt="example"
                                                src={`${jobDetail.company?.logo}`}
                                            />
                                        </Link>
                                    </div>
                                    <div>
                                        <Link to={`/company/${jobDetail.company?.name ? convertSlug(jobDetail.company.name) : ''}?id=${jobDetail.company?._id}`}>
                                            {jobDetail.company?.name}
                                        </Link>
                                    </div>
                                </div>
                                {/* Gợi ý job theo skills liên quan */}
                                <div style={{ width: '100%', marginTop: 24 }}>
                                    <h2 style={{ fontSize: 18, marginBottom: 12 }}>Công việc liên quan</h2>
                                    <JobCard
                                        showPagination={false}
                                        filter={`skills=${encodeURIComponent((jobDetail?.skills?.[0] || '').toLowerCase())}&excludeId=${jobDetail?._id}&scope=public`}
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
            <ApplyModal
                isModalOpen={isModalOpen}
                setIsModalOpen={setIsModalOpen}
                jobDetail={jobDetail}
            />
        </div>
    )
}
export default ClientJobDetailPage;