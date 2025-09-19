import { callFetchJob } from '@/config/api';
import { convertSlug, getLocationName } from '@/config/utils';
import { IJob } from '@/types/backend';
import { EnvironmentOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { Card, Col, Empty, Pagination, Row, Spin, Tag, Tooltip } from 'antd';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { useEffect, useLayoutEffect, useRef, useState } from 'react';
import { isMobile } from 'react-device-detect';
import { Link, useNavigate } from 'react-router-dom';
import styles from 'styles/client.module.scss';
dayjs.extend(relativeTime)

interface IProps {
    showPagination?: boolean;
    filter?: string;
    defaultCurrent?: number;
    defaultPageSize?: number;
    onPageChange?: (current: number, pageSize: number) => void;
    hideHeader?: boolean;
    listDirection?: 'grid' | 'vertical';
}

const JobCard = (props: IProps) => {
    const { showPagination = false, filter: externalFilter, defaultCurrent, defaultPageSize, onPageChange, hideHeader = false, listDirection = 'grid' } = props;

    const [displayJob, setDisplayJob] = useState<IJob[] | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(false);

    const [current, setCurrent] = useState(defaultCurrent || 1);
    const [pageSize, setPageSize] = useState(defaultPageSize || (showPagination ? 8 : 6));
    const [total, setTotal] = useState(0);
    const [filter, setFilter] = useState(externalFilter || "");
    const [sortQuery, setSortQuery] = useState("sort=-createdAt");
    const navigate = useNavigate();
    const latestQueryRef = (function () { return { current: '' as string }; })();

    const SkillsRow = ({ skills, jobId }: { skills: string[]; jobId: string }) => {
        const containerRef = useRef<HTMLDivElement | null>(null);
        const tagRefs = useRef<HTMLElement[]>([]);
        const [visibleCount, setVisibleCount] = useState<number>(skills.length);
        const [measuring, setMeasuring] = useState<boolean>(true);

        const measure = () => {
            const nodes = tagRefs.current;
            if (!nodes || nodes.length === 0) { setMeasuring(false); return; }
            const firstTop = nodes[0].offsetTop;
            let count = nodes.length;
            for (let i = 1; i < nodes.length; i++) {
                if (nodes[i].offsetTop > firstTop) { count = i; break; }
            }
            // Reserve space for +n tag if overflow
            if (count < nodes.length) count = Math.max(0, count - 1);
            setVisibleCount(count);
            setMeasuring(false);
        };

        useLayoutEffect(() => {
            setMeasuring(true);
        }, [skills]);

        useLayoutEffect(() => {
            if (measuring) {
                // delay a frame to ensure DOM laid out
                requestAnimationFrame(() => measure());
            }
        }, [measuring]);

        useEffect(() => {
            const onResize = () => {
                setMeasuring(true);
            };
            window.addEventListener('resize', onResize);
            return () => window.removeEventListener('resize', onResize);
        }, []);

        // reset refs each render
        tagRefs.current = [];
        const setTagRef = (el: any) => { if (el) tagRefs.current.push(el as HTMLElement); };

        const visibleSkills = measuring ? skills : skills.slice(0, visibleCount);
        const overflowSkills = measuring ? [] : skills.slice(visibleCount);

        return (
            <div
                className={styles["job-skills"]}
                ref={containerRef}
                style={{ display: 'flex', flexWrap: measuring ? 'wrap' : 'nowrap', overflow: 'hidden' }}
            >
                {visibleSkills.map((s, idx) => (
                    <Tag key={`${jobId}-sk-${s}-${idx}`} color="gold" style={{ marginBottom: 6 }} ref={setTagRef as any}>
                        {s}
                    </Tag>
                ))}
                {!measuring && overflowSkills.length > 0 && (
                    <Tooltip title={overflowSkills.join(', ')}>
                        <Tag style={{ marginBottom: 6, cursor: 'default' }}>+{overflowSkills.length}</Tag>
                    </Tooltip>
                )}
            </div>
        );
    };

    useEffect(() => {
        setFilter(externalFilter || "");
        setCurrent(1);
    }, [externalFilter]);

    // Đồng bộ state phân trang với giá trị từ URL (props)
    useEffect(() => {
        if (typeof defaultCurrent === 'number' && defaultCurrent > 0 && defaultCurrent !== current) {
            setCurrent(defaultCurrent);
        }
    }, [defaultCurrent]);
    useEffect(() => {
        if (typeof defaultPageSize === 'number' && defaultPageSize > 0 && defaultPageSize !== pageSize) {
            setPageSize(defaultPageSize);
        }
    }, [defaultPageSize]);

    useEffect(() => {
        fetchJob();
    }, [current, pageSize, filter, sortQuery]);

    const fetchJob = async () => {
        setIsLoading(true)
        let query = `current=${current}&pageSize=${pageSize}`;
        if (filter) {
            query += `&${filter}`;
        }
        if (!/scope=/.test(query)) {
            query += `&scope=public`;
        }
        if (sortQuery) {
            query += `&${sortQuery}`;
        }

        latestQueryRef.current = query;
        const res = await callFetchJob(query);
        // tránh ghi đè bởi response cũ
        if (latestQueryRef.current === query) {
            if (res && res.data) {
                setDisplayJob(res.data.result);
                setTotal(res.data.meta.total)
            }
            setIsLoading(false)
        }
    }



    const handleOnchangePage = (pagination: { current: number, pageSize: number }) => {
        if (pagination && pagination.current !== current) {
            setCurrent(pagination.current)
        }
        if (pagination && pagination.pageSize !== pageSize) {
            setPageSize(pagination.pageSize)
            setCurrent(1);
        }
        if (onPageChange) {
            const nextCurrent = (pagination && pagination.current !== current) ? pagination.current : current;
            const nextPageSize = (pagination && pagination.pageSize !== pageSize) ? pagination.pageSize : pageSize;
            onPageChange(nextCurrent, nextPageSize);
        }
    }

    const handleViewDetailJob = (item: IJob) => {
        const slug = convertSlug(item.name);
        navigate(`/job/${slug}?id=${item._id}`)
    }

    return (
        <div className={`${styles["card-job-section"]}`}>
            <div className={`${styles["job-content"]}`}>
                <Spin spinning={isLoading} tip="Loading...">
                    <Row gutter={[20, 20]}>
                        {!hideHeader && (
                            <Col span={24}>
                                <div className={isMobile ? styles["dflex-mobile"] : styles["dflex-pc"]}>
                                    <h3 className={styles["title"]}>Công Việc Mới Nhất</h3>
                                    {!showPagination && (
                                        <Link to="/job">Xem tất cả</Link>
                                    )}
                                </div>
                            </Col>
                        )}

                        {displayJob?.map(item => {
                            return (
                                <Col span={24} md={listDirection === 'vertical' ? 24 : 12} key={item._id}>
                                    <Card
                                        hoverable
                                        onClick={() => handleViewDetailJob(item)}
                                        style={{ height: '100%' }}
                                        bodyStyle={{ padding: 16 }}
                                    >
                                        <div className={styles["card-job-content"]}>
                                            <div className={styles["card-job-left"]}>
                                                <img alt="company" src={`${item?.company?.logo}`} />
                                            </div>
                                            <div className={styles["card-job-right"]}>
                                                <div className={styles["job-title"]}>{item.name}</div>
                                                {item?.skills && item.skills.length > 0 && (
                                                    <SkillsRow skills={item.skills} jobId={item._id!} />
                                                )}
                                                <div className={styles["job-location"]}><EnvironmentOutlined style={{ color: '#58aaab' }} />&nbsp;{getLocationName(item.location)}</div>
                                                <div><ThunderboltOutlined style={{ color: 'orange' }} />&nbsp;{(item.salary + "")?.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} đ</div>
                                                <div className={styles["job-updatedAt"]}>{dayjs(item.updatedAt).fromNow()}</div>
                                            </div>
                                        </div>
                                    </Card>
                                </Col>
                            )
                        })}


                        {(!displayJob || displayJob && displayJob.length === 0)
                            && !isLoading &&
                            <div className={styles["empty"]}>
                                <Empty description="Không có dữ liệu" />
                            </div>
                        }
                    </Row>
                    {showPagination && <>
                        <div style={{ marginTop: 30 }}></div>
                        <Row style={{ display: "flex", justifyContent: "center" }}>
                            <Pagination
                                current={current}
                                total={total}
                                pageSize={pageSize}
                                responsive
                                onChange={(p: number, s: number) => handleOnchangePage({ current: p, pageSize: s })}
                            />
                        </Row>
                    </>}
                </Spin>
            </div>
        </div>
    )
}

export default JobCard;