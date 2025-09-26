import { callFetchCompany } from '@/config/api';
import { convertSlug } from '@/config/utils';
import { ICompany } from '@/types/backend';
import { EnvironmentOutlined } from '@ant-design/icons';
import { Col, Empty, Pagination, Row, Spin, Tooltip } from 'antd';
import { useEffect, useState } from 'react';
import { isMobile } from 'react-device-detect';
import { Link, useNavigate } from 'react-router-dom';
import styles from 'styles/client.module.scss';

interface IProps {
    showPagination?: boolean;
    defaultCurrent?: number;
    defaultPageSize?: number;
    onPageChange?: (current: number, pageSize: number) => void;
    filter?: string;
    ready?: boolean;
}

const CompanyCard = (props: IProps) => {
    const { showPagination = false, defaultCurrent, defaultPageSize, onPageChange, filter: externalFilter, ready = true } = props;

    const [displayCompany, setDisplayCompany] = useState<ICompany[] | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(false);

    const [current, setCurrent] = useState(defaultCurrent || 1);
    const [pageSize, setPageSize] = useState(defaultPageSize || (showPagination ? 8 : 4));
    const [total, setTotal] = useState(0);
    const [filter, setFilter] = useState<string | null>(null);
    const [sortQuery, setSortQuery] = useState("sort=-createdAt");
    const navigate = useNavigate();

    const lastFetchedRef = (function () { return { current: '' as string }; })();
    useEffect(() => {
        if (!ready) return;
        if (filter === null) return;
        let query = `current=${current}&pageSize=${pageSize}&scope=public`;
        if (filter) query += `&${filter}`;
        if (sortQuery) query += `&${sortQuery}`;
        if (lastFetchedRef.current === query) return;
        lastFetchedRef.current = query;
        fetchCompany(query);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [ready, current, pageSize, filter, sortQuery]);

    // Đồng bộ state phân trang với giá trị từ URL (props)
    useEffect(() => {
        if (typeof defaultCurrent === 'number' && defaultCurrent > 0 && defaultCurrent !== current) {
            setCurrent(defaultCurrent);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [defaultCurrent]);
    useEffect(() => {
        if (typeof defaultPageSize === 'number' && defaultPageSize > 0 && defaultPageSize !== pageSize) {
            setPageSize(defaultPageSize);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [defaultPageSize]);

    // nhận filter từ props (URL)
    useEffect(() => {
        if (externalFilter !== undefined) {
            const f = externalFilter ? `name=${externalFilter}` : "";
            setFilter(f);
            setCurrent(1);
        } else {
            // Home: cho phép fetch ngay không filter
            setFilter("");
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [externalFilter]);

    const fetchCompany = async (query: string) => {
        setIsLoading(true)
        const res = await callFetchCompany(query);
        if (res && res.data) {
            setDisplayCompany(res.data.result);
            setTotal(res.data.meta.total)
        }
        setIsLoading(false)
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

    const handleViewDetailJob = (item: ICompany) => {
        if (item.name) {
            const slug = convertSlug(item.name);
            navigate(`/company/${slug}?id=${item._id}`)
        }
    }

    return (
        <div className={`${styles["company-section"]}`}>
            <div className={styles["company-content"]}>
                <Spin spinning={isLoading} tip="Loading...">
                    <Row gutter={[16, 16]} style={{ margin: 0 }}>
                        <Col span={24}>

                            <div className={isMobile ? styles["dflex-mobile"] : styles["dflex-pc"]}>
                                <h2 className={styles["title"]}>Nhà Tuyển Dụng Hàng Đầu</h2>
                                {!showPagination && (
                                    <Link to="/company">Xem tất cả</Link>
                                )}
                            </div>
                        </Col>

                        {displayCompany?.map(item => {
                            return (
                                <Col span={12} md={6} key={item._id} style={{ marginBottom: 16 }}>
                                    <div style={{
                                        background: 'var(--surface)',
                                        border: '1px solid var(--border)',
                                        borderRadius: 8,
                                        overflow: 'hidden',
                                        cursor: 'pointer',
                                        transition: 'transform 0.2s ease',
                                        height: 300,
                                        width: '100%',
                                        display: 'flex',
                                        flexDirection: 'column'
                                    }}
                                        onClick={() => handleViewDetailJob(item)}
                                        onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.02)'}
                                        onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
                                    >
                                        {/* Logo */}
                                        <div style={{
                                            height: 220,
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'center',
                                            background: 'var(--input-bg)',
                                            padding: 16,
                                            flexDirection: 'column',
                                            gap: 16
                                        }}>
                                            <img
                                                alt={item.name}
                                                src={item?.logo}
                                                style={{
                                                    width: 160,
                                                    height: 160,
                                                    objectFit: 'contain'
                                                }}
                                            />
                                            <div style={{
                                                fontSize: 18,
                                                fontWeight: 700,
                                                color: 'var(--title-text)',
                                                textAlign: 'center',
                                                lineHeight: 1.3,
                                                width: '100%',
                                                overflow: 'hidden',
                                                textOverflow: 'ellipsis',
                                                whiteSpace: 'nowrap'
                                            }}>
                                                {item.name}
                                            </div>
                                        </div>
                                        {/* Content */}
                                        <div style={{
                                            flex: 1,
                                            padding: '12px 16px',
                                            display: 'flex',
                                            flexDirection: 'column',
                                            gap: 8,
                                            borderTop: '1px solid var(--border)'
                                        }}>
                                            {/* footer bar */}
                                            <div style={{ marginTop: 'auto', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12 }}>
                                                <div style={{ display: 'flex', alignItems: 'center', gap: 6, minWidth: 0, flex: 1 }}>
                                                    <EnvironmentOutlined style={{ color: '#58aaab' }} />
                                                    <Tooltip title={item.address} mouseEnterDelay={0.2} placement="top">
                                                        <span style={{
                                                            color: 'var(--sub-text)',
                                                            maxWidth: '100%',
                                                            overflow: 'hidden',
                                                            textOverflow: 'ellipsis',
                                                            whiteSpace: 'nowrap',
                                                            display: 'inline-block'
                                                        }}>{item.address}</span>
                                                    </Tooltip>
                                                </div>
                                                <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexShrink: 0, whiteSpace: 'nowrap' }}>
                                                    <span className={styles["pulse-dot"]} />
                                                    <span style={{ fontWeight: 600 }}>{(item.openJobs ?? 0).toLocaleString('vi-VN')} Job</span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </Col>
                            )
                        })}

                        {(!displayCompany || displayCompany && displayCompany.length === 0)
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

export default CompanyCard;