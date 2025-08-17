import { callAiSuggestJobs } from '@/config/api';
import { getLocationName } from '@/config/utils';
import { IJob } from '@/types/backend';
import { BulbOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { Button, Drawer, FloatButton, List, Tag, Typography } from 'antd';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const AiSuggestWidget = () => {
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [jobs, setJobs] = useState<IJob[]>([]);
  const [reasoning, setReasoning] = useState<string>('');
  const [query, setQuery] = useState<string>('');
  const navigate = useNavigate();

  const fetchSuggest = async () => {
    setLoading(true);
    try {
      const res = await callAiSuggestJobs({ query });
      const payload = res?.data;
      const list = (payload as any)?.jobs?.result || [];
      setJobs(list);
      const rea = (payload as any)?.reasoning || '';
      setReasoning(rea);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <FloatButton
        icon={<BulbOutlined />}
        onClick={() => setOpen(true)}
        tooltip='Gợi ý jobs'
        style={{ right: 96 }}
      />
      <Drawer title="Gợi ý việc làm bởi AI" width={520} onClose={() => setOpen(false)} open={open}
        extra={<Button type='primary' icon={<ThunderboltOutlined />} loading={loading} onClick={fetchSuggest}>Tạo gợi ý</Button>}
      >
        <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="VD: React Hà Nội lương > 20tr"
            style={{ flex: 1, border: '1px solid #eee', padding: 8, borderRadius: 6 }}
          />
          <Button onClick={fetchSuggest} loading={loading}>Tìm</Button>
        </div>
        {reasoning && <Typography.Paragraph type='secondary'>{reasoning}</Typography.Paragraph>}
        <List
          loading={loading}
          dataSource={jobs}
          renderItem={(item: IJob) => (
            <List.Item onClick={() => navigate(`/job/${encodeURIComponent(item.name)}?id=${item._id}`)} style={{ cursor: 'pointer' }}>
              <List.Item.Meta
                title={<span>{item.name} <Tag color='blue'>{getLocationName(item.location)}</Tag></span>}
                description={<span>{item.company?.name}</span>}
              />
            </List.Item>
          )}
        />
      </Drawer>
    </>
  );
};

export default AiSuggestWidget;


