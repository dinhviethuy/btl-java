import JobCard from '@/components/client/card/job.card';
import SearchClient from '@/components/client/search.client';
import { Col, Divider, Row } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import styles from 'styles/client.module.scss';

const ClientJobPage = (props: any) => {
    const location = useLocation();
    const navigate = useNavigate();
    useEffect(() => { document.title = 'Việc làm IT'; }, []);
    const [current, setCurrent] = useState<number>(1);
    const [pageSize, setPageSize] = useState<number>(8);

    // parse URL on first load & changes (derive filter without intermediate state to tránh nháy)
    const urlFilter = useMemo(() => {
        const params = new URLSearchParams(location.search);
        const skills = params.getAll('skills');
        const locations = params.getAll('locations');
        const companyId = params.get('companyId');
        const name = params.get('name');
        let q = '';
        if (skills.length) q += skills.map(s => `skills=${encodeURIComponent(s)}`).join('&');
        if (locations.length) q += (q ? '&' : '') + locations.map(l => `locations=${encodeURIComponent(l)}`).join('&');
        if (companyId) q += (q ? '&' : '') + `companyId=${encodeURIComponent(companyId)}`;
        if (name) q += (q ? '&' : '') + `name=${encodeURIComponent(name)}`;
        return q;
    }, [location.search]);

    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const c = parseInt(params.get('page') || '1');
        const ps = pageSize; // giữ pageSize hiện tại
        setCurrent(isNaN(c) ? 1 : c);
        setPageSize(isNaN(ps) ? 8 : ps);
    }, [location.search]);

    const handleSearch = (q: string) => {
        const params = new URLSearchParams(location.search);
        // reset paging when search
        params.set('page', '1');
        // không lưu pageSize lên URL
        params.delete('skills');
        params.delete('locations');
        params.delete('companyId');
        params.delete('name');
        if (q) {
            q.split('&').forEach(pair => {
                const [k, v] = pair.split('=');
                if (k === 'skills' || k === 'locations' || k === 'companyId' || k === 'name') params.append(k, decodeURIComponent(v));
            });
        }
        navigate({ pathname: '/job', search: params.toString() }, { replace: true });
    };

    const handlePageChange = (c: number, ps: number) => {
        const params = new URLSearchParams(location.search);
        params.set('page', c.toString());
        // không lưu pageSize lên URL
        navigate({ pathname: '/job', search: params.toString() }, { replace: true });
    };
    return (
        <div className={styles["container"]} style={{ marginTop: 20 }}>
            <Row gutter={[20, 20]}>
                <Col span={24}>
                    <SearchClient onSearch={handleSearch} />
                </Col>
                <Divider />

                <Col span={24}>
                    <JobCard
                        showPagination={true}
                        filter={urlFilter}
                        defaultCurrent={current}
                        defaultPageSize={pageSize}
                        onPageChange={handlePageChange}
                    />
                </Col>
            </Row>
        </div>
    )
}

export default ClientJobPage;