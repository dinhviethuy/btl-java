import JobCard from '@/components/client/card/job.card';
import SearchClient from '@/components/client/search.client';
import { Col, Divider, Row } from 'antd';
import { useState } from 'react';
import styles from 'styles/client.module.scss';

const ClientJobPage = (props: any) => {
    const [filter, setFilter] = useState("");
    return (
        <div className={styles["container"]} style={{ marginTop: 20 }}>
            <Row gutter={[20, 20]}>
                <Col span={24}>
                    <SearchClient onSearch={setFilter} />
                </Col>
                <Divider />

                <Col span={24}>
                    <JobCard
                        showPagination={true}
                        filter={filter}
                    />
                </Col>
            </Row>
        </div>
    )
}

export default ClientJobPage;