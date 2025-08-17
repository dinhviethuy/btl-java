import { callAiChat, callAiSuggestJobs, callGetSubscriberSkills } from '@/config/api';
import { LOCATION_LIST, SKILLS_LIST, getLocationName } from '@/config/utils';
import { IJob } from '@/types/backend';
import { MessageOutlined, SendOutlined } from '@ant-design/icons';
import { Button, Card, FloatButton, Input, List, Space, Tag, Typography, message } from 'antd';
import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';

interface ChatItem { role: 'user' | 'assistant'; content?: string; jobs?: IJob[] }

const AiChatWidget = () => {
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [history, setHistory] = useState<ChatItem[]>([
    { role: 'assistant', content: 'Xin chào! Tôi có thể giúp bạn tìm việc theo kỹ năng, địa điểm, hoặc gợi ý phù hợp.' }
  ]);
  const listRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (listRef.current) {
      listRef.current.scrollTop = listRef.current.scrollHeight;
    }
  }, [history, open]);

  const sendChat = async () => {
    const msg = input.trim();
    if (!msg) return;
    setHistory((h) => [...h, { role: 'user', content: msg }]);
    setInput('');
    setLoading(true);
    try {
      const [chatRes, suggestRes] = await Promise.all([
        callAiChat(msg),
        callAiSuggestJobs({ query: msg })
      ]);
      const reply = chatRes?.data?.reply || 'Xin lỗi, tôi chưa hiểu. Hãy thử cách khác.';
      const jobs = (suggestRes as any)?.data?.jobs?.result || [];
      setHistory((h) => {
        const next = [...h, { role: 'assistant', content: reply } as any];
        if (Array.isArray(jobs) && jobs.length > 0) {
          next.push({ role: 'assistant', content: 'Gợi ý việc làm theo yêu cầu của bạn:', jobs } as any);
        }
        return next;
      });
    } finally {
      setLoading(false);
    }
  };

  const suggestFromText = async (text?: string) => {
    const q = (text ?? input).trim();
    if (!q) return message.info('Nhập nội dung trước khi gợi ý.');
    setLoading(true);
    try {
      const res = await callAiSuggestJobs({ query: q });
      const jobs = (res as any)?.data?.jobs?.result || [];
      setHistory((h) => [...h, { role: 'assistant', content: 'Một vài gợi ý phù hợp:', jobs }]);
    } finally {
      setLoading(false);
    }
  };

  const suggestFromMySkills = async () => {
    setLoading(true);
    try {
      const skillsRes = await callGetSubscriberSkills();
      const skills = (skillsRes as any)?.data?.skills || [];
      if (!skills.length) {
        setHistory((h) => [...h, { role: 'assistant', content: 'Bạn chưa đăng ký kỹ năng. Vào mục đăng ký nhận job để thêm kỹ năng nhé.' }]);
        return;
      }
      const res = await callAiSuggestJobs({ query: 'gợi ý theo kỹ năng của tôi', skills });
      const jobs = (res as any)?.data?.jobs?.result || [];
      setHistory((h) => [...h, { role: 'assistant', content: 'Gợi ý theo kỹ năng đã đăng ký:', jobs }]);
    } catch (e) {
      message.error('Bạn cần đăng nhập để sử dụng tính năng này.');
    } finally {
      setLoading(false);
    }
  };

  const clearChat = () => setHistory([{ role: 'assistant', content: 'Xin chào! Tôi có thể giúp bạn tìm việc theo kỹ năng, địa điểm, hoặc gợi ý phù hợp.' }]);

  return (
    <>
      <FloatButton
        icon={<MessageOutlined />}
        onClick={() => setOpen(!open)}
        tooltip={open ? 'Đóng AI' : 'Chat AI / Gợi ý việc làm'}
      />
      {open && (
        <div style={{ position: 'fixed', right: 24, bottom: 80, width: 360, zIndex: 1000 }}>
          <Card
            title={<span>Trợ lý việc làm</span>}
            extra={<Button size='small' onClick={() => setOpen(false)}>Đóng</Button>}
            bodyStyle={{ padding: 8 }}
          >
            <div ref={listRef} style={{ height: 320, overflowY: 'auto', padding: 8, background: '#fafafa', border: '1px solid #f0f0f0', borderRadius: 6 }}>
              <List
                dataSource={history}
                renderItem={(item) => (
                  <List.Item style={{ border: 'none', padding: '6px 0' }}>
                    <div style={{ width: '100%' }}>
                      {item.jobs && item.jobs.length > 0 ? (
                        <div style={{
                          background: '#fff',
                          padding: '8px 10px',
                          borderRadius: 8,
                          boxShadow: '0 1px 2px rgba(0,0,0,0.05)'
                        }}>
                          {item.content && <Typography.Text strong>{item.content}</Typography.Text>}
                          <List
                            style={{ marginTop: 8 }}
                            size='small'
                            dataSource={item.jobs.slice(0, 5)}
                            renderItem={(job: IJob) => (
                              <List.Item style={{ cursor: 'pointer' }} onClick={() => navigate(`/job/${encodeURIComponent(job.name)}?id=${job._id}`)}>
                                <div>
                                  <div style={{ fontWeight: 500 }}>{job.name} <Tag color='blue'>{getLocationName(job.location)}</Tag></div>
                                  <div style={{ color: '#666', fontSize: 12 }}>{job.company?.name}</div>
                                </div>
                              </List.Item>
                            )}
                          />
                        </div>
                      ) : (
                        <div style={{
                          maxWidth: '85%',
                          marginLeft: item.role === 'assistant' ? 0 : 'auto',
                          background: item.role === 'assistant' ? '#fff' : '#1677ff',
                          color: item.role === 'assistant' ? '#000' : '#fff',
                          padding: '8px 10px',
                          borderRadius: 8,
                          boxShadow: '0 1px 2px rgba(0,0,0,0.05)'
                        }}>
                          <Typography.Text>{item.content}</Typography.Text>
                        </div>
                      )}
                    </div>
                  </List.Item>
                )}
              />
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 8 }}>
              <Space wrap>
                <Button size='small' onClick={() => suggestFromText()} loading={loading}>Gợi ý jobs</Button>
                <Button size='small' onClick={suggestFromMySkills} loading={loading}>Theo kỹ năng của tôi</Button>
                <Button size='small' onClick={clearChat} disabled={loading}>Xóa chat</Button>
              </Space>
            </div>
            <div style={{ marginTop: 8 }}>
              <Space wrap>
                {SKILLS_LIST.slice(0, 6).map((s) => (
                  <Tag key={s.value} color='processing' style={{ cursor: 'pointer' }} onClick={() => setInput((v) => (v ? v + ' ' + s.label : s.label))}>{s.label}</Tag>
                ))}
              </Space>
            </div>
            <div style={{ marginTop: 6 }}>
              <Space wrap>
                {LOCATION_LIST.slice(0, 4).map((l) => (
                  <Tag key={l.value} color='blue' style={{ cursor: 'pointer' }} onClick={() => setInput((v) => (v ? v + ' ' + l.label : l.label))}>{l.label}</Tag>
                ))}
              </Space>
            </div>
            <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
              <Input
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onPressEnter={sendChat}
                placeholder="Hỏi AI về việc làm..."
                disabled={loading}
              />
              <Button type='primary' icon={<SendOutlined />} loading={loading} onClick={sendChat} />
            </div>
          </Card>
        </div>
      )}
    </>
  );
};

export default AiChatWidget;


