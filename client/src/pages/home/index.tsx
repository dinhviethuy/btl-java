import CompanyCard from '@/components/client/card/company.card';
import JobCard from '@/components/client/card/job.card';
import SearchClient from '@/components/client/search.client';
import { Divider } from 'antd';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from 'styles/client.module.scss';

const HomePage = () => {
    const [filter, setFilter] = useState("");
    const navigate = useNavigate();
    const defaultPageSize = 6; // Home list page size
    const defaultCurrent = 1;

    const handleSearch = (q: string) => {
        // Điều hướng sang trang /job với query, kèm current/pageSize
        const params = new URLSearchParams();
        params.set('page', defaultCurrent.toString());
        // không đẩy pageSize lên URL
        if (q) {
            q.split('&').forEach(pair => {
                const [k, v] = pair.split('=');
                if (k === 'skills' || k === 'locations') params.append(k, decodeURIComponent(v));
            });
        }
        navigate({ pathname: '/job', search: params.toString() });
    };
    return (
        <div className={`${styles["container"]} ${styles["home-section"]}`}>
            <div className="search-content" style={{ marginTop: 20 }}>
                <SearchClient onSearch={handleSearch} />
            </div>
            <Divider />
            <CompanyCard />
            <div style={{ margin: 50 }}></div>
            <Divider />
            <JobCard filter={filter} />
        </div>
    )
}

export default HomePage;