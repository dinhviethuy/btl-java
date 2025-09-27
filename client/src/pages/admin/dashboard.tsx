import { callGetStatsOverview, callGetStatsTimeSeries, callGetTopCompanies } from '@/config/api';
import { useAppSelector } from '@/redux/hooks';
import { Area, Bar, Column, Line, Pie } from '@ant-design/plots';
import { Avatar, Button, Card, Checkbox, Col, DatePicker, List, Radio, Row, Space, Statistic } from "antd";
import dayjs from 'dayjs';
import { useEffect, useState } from 'react';
import CountUp from 'react-countup';

const DashboardPage = () => {
    useEffect(() => { document.title = 'Bảng điều khiển'; }, []);
    const formatter = (value: number | string) => {
        return (
            <CountUp end={Number(value)} separator="," />
        );
    };

    const roleName = useAppSelector(state => {
        const r = state.account.user?.role as any;
        return r && typeof r === 'object' ? (r.name || "") : "";
    });
    const [overview, setOverview] = useState<any>({});
    const [ts, setTs] = useState<any>({});
    const [topCompanies, setTopCompanies] = useState<any[]>([]);
    const [rangeDays, setRangeDays] = useState<number>(30);
    const [chartType, setChartType] = useState<'line' | 'area' | 'column'>('line');
    const [loading, setLoading] = useState<boolean>(false);
    const [statusChartType, setStatusChartType] = useState<'pie' | 'bar' | 'column'>('pie');
    const [plotTheme, setPlotTheme] = useState<any>('classic');
    const [customDateRange, setCustomDateRange] = useState<[dayjs.Dayjs | null, dayjs.Dayjs | null] | null>(null);
    const [dateRangeType, setDateRangeType] = useState<'preset' | 'custom'>('preset');
    const STATUS_KEYS = ['PENDING', 'REVIEWING', 'APPROVED', 'REJECTED'];
    const labelStatus: any = { PENDING: 'Chờ duyệt', REVIEWING: 'Đang xem', APPROVED: 'Đã duyệt', REJECTED: 'Từ chối' };
    const [statusFilter, setStatusFilter] = useState<string[]>([...STATUS_KEYS]);

    useEffect(() => {
        const load = async () => {
            try {
                setLoading(true);

                // Tính số ngày dựa trên loại date range
                let days: number;
                if (dateRangeType === 'custom' && customDateRange && customDateRange[0] && customDateRange[1]) {
                    const [start, end] = customDateRange;
                    days = end.diff(start, 'day') + 1;
                } else {
                    days = rangeDays;
                }

                const isUpperAdmin = !!roleName && (roleName.toUpperCase() === 'ADMIN' || roleName.toUpperCase() === 'SUPER_ADMIN');
                const reqOverview = callGetStatsOverview(days);
                const reqTs = callGetStatsTimeSeries(days);
                const [o, t] = await Promise.all([reqOverview, reqTs]);
                setOverview(o?.data || {});
                setTs(t?.data || {});
                if (isUpperAdmin) {
                    const top = await callGetTopCompanies(5);
                    setTopCompanies(top?.data || []);
                } else {
                    setTopCompanies([]);
                }
            } finally {
                setLoading(false);
            }
        };
        load();
        try {
            const themeAttr = document.documentElement.getAttribute('data-theme');
            setPlotTheme(themeAttr === 'dark' ? 'classicDark' : 'classic');
        } catch { }
    }, [rangeDays, dateRangeType, customDateRange, roleName]);

    useEffect(() => {
        if (typeof document === 'undefined') return;
        const el = document.documentElement;
        const updateTheme = () => {
            const themeAttr = el.getAttribute('data-theme');
            setPlotTheme(themeAttr === 'dark' ? 'classicDark' : 'classic');
        };
        const observer = new MutationObserver((mutations) => {
            for (const m of mutations) {
                if (m.type === 'attributes' && m.attributeName === 'data-theme') {
                    updateTheme();
                }
            }
        });
        observer.observe(el, { attributes: true, attributeFilter: ['data-theme'] });
        // sync ngay lần đầu
        updateTheme();
        return () => observer.disconnect();
    }, []);

    return (
        <div style={{ padding: 8 }}>
            <Row gutter={[16, 16]}>
                <Col span={24}>
                    <Card style={{ marginBottom: 16 }}>
                        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                            {/* Row 1: Time Range, Chart Type, Status Chart Type */}
                            <Row gutter={[16, 16]} align="top" style={{ display: 'flex', alignItems: 'center' }}>
                                <Col xs={24} sm={24} md={8} lg={12} xl={8}>
                                    <div>
                                        <div style={{ marginBottom: 8, fontWeight: 500 }}>Khoảng thời gian</div>
                                        <Space wrap>
                                            <Radio.Group
                                                value={dateRangeType}
                                                onChange={(e) => {
                                                    setDateRangeType(e.target.value);
                                                    if (e.target.value === 'preset') {
                                                        setCustomDateRange(null);
                                                    }
                                                }}
                                            >
                                                <Radio.Button value="preset">Cố định</Radio.Button>
                                                <Radio.Button value="custom">Tùy chỉnh</Radio.Button>
                                            </Radio.Group>

                                            {dateRangeType === 'preset' ? (
                                                <Radio.Group
                                                    value={rangeDays}
                                                    onChange={(e) => setRangeDays(e.target.value)}
                                                >
                                                    <Radio.Button value={7}>7 ngày</Radio.Button>
                                                    <Radio.Button value={30}>30 ngày</Radio.Button>
                                                    <Radio.Button value={90}>90 ngày</Radio.Button>
                                                </Radio.Group>
                                            ) : (
                                                <DatePicker.RangePicker
                                                    value={customDateRange}
                                                    onChange={(dates) => setCustomDateRange(dates)}
                                                    format="DD/MM/YYYY"
                                                    placeholder={['Từ ngày', 'Đến ngày']}
                                                    presets={[
                                                        {
                                                            label: '7 ngày gần đây',
                                                            value: [dayjs().subtract(6, 'day'), dayjs()],
                                                        },
                                                        {
                                                            label: '30 ngày gần đây',
                                                            value: [dayjs().subtract(29, 'day'), dayjs()],
                                                        },
                                                        {
                                                            label: '90 ngày gần đây',
                                                            value: [dayjs().subtract(89, 'day'), dayjs()],
                                                        },
                                                        {
                                                            label: 'Tháng này',
                                                            value: [dayjs().startOf('month'), dayjs().endOf('month')],
                                                        },
                                                        {
                                                            label: 'Tháng trước',
                                                            value: [dayjs().subtract(1, 'month').startOf('month'), dayjs().subtract(1, 'month').endOf('month')],
                                                        },
                                                    ]}
                                                />
                                            )}
                                        </Space>
                                    </div>
                                </Col>
                                <Col xs={24} sm={24} md={3} lg={6} xl={3}>
                                    <div>
                                        <div style={{ marginBottom: 8, fontWeight: 500 }}>Loại biểu đồ</div>
                                        <Radio.Group value={chartType} onChange={(e) => setChartType(e.target.value)} size="small">
                                            <Radio.Button value='line'>Line</Radio.Button>
                                            <Radio.Button value='area'>Area</Radio.Button>
                                            <Radio.Button value='column'>Column</Radio.Button>
                                        </Radio.Group>
                                    </div>
                                </Col>
                                <Col xs={24} sm={24} md={3} lg={6} xl={3}>
                                    <div>
                                        <div style={{ marginBottom: 8, fontWeight: 500 }}>Biểu đồ trạng thái</div>
                                        <Radio.Group value={statusChartType} onChange={(e) => setStatusChartType(e.target.value)} size="small">
                                            <Radio.Button value='pie'>Pie</Radio.Button>
                                            <Radio.Button value='bar'>Bar</Radio.Button>
                                            <Radio.Button value='column'>Column</Radio.Button>
                                        </Radio.Group>
                                    </div>
                                </Col>
                                <Col xs={24} sm={24} md={8} lg={12} xl={8}>
                                    <div>
                                        <div style={{ marginBottom: 8, fontWeight: 500 }}>Hiển thị trạng thái</div>
                                        <Checkbox.Group
                                            options={STATUS_KEYS.map(k => ({ label: labelStatus[k], value: k }))}
                                            value={statusFilter}
                                            onChange={(list) => setStatusFilter(list as string[])}
                                        />
                                    </div>
                                </Col>
                                <Col xs={24} sm={24} md={2} lg={12} xl={2}>
                                    <div style={{ display: 'flex', alignItems: 'center', height: '100%' }}>
                                        <Button
                                            loading={loading}
                                            onClick={() => {
                                                const load = async () => {
                                                    try {
                                                        setLoading(true);

                                                        let days: number;
                                                        if (dateRangeType === 'custom' && customDateRange && customDateRange[0] && customDateRange[1]) {
                                                            const [start, end] = customDateRange;
                                                            days = end.diff(start, 'day') + 1;
                                                        } else {
                                                            days = rangeDays;
                                                        }

                                                        const isUpperAdmin = !!roleName && (roleName.toUpperCase() === 'ADMIN' || roleName.toUpperCase() === 'SUPER_ADMIN');
                                                        const reqOverview = callGetStatsOverview(days);
                                                        const reqTs = callGetStatsTimeSeries(days);
                                                        const [o, t] = await Promise.all([reqOverview, reqTs]);
                                                        setOverview(o?.data || {});
                                                        setTs(t?.data || {});
                                                        if (isUpperAdmin) {
                                                            const top = await callGetTopCompanies(5);
                                                            setTopCompanies(top?.data || []);
                                                        } else {
                                                            setTopCompanies([]);
                                                        }
                                                    } finally {
                                                        setLoading(false);
                                                    }
                                                };
                                                load();
                                            }}
                                            type="primary"
                                        >
                                            Reload
                                        </Button>
                                    </div>
                                </Col>
                            </Row>
                        </Space>
                    </Card>
                </Col>
                <Col span={24} md={6}>
                    <Card hoverable>
                        <Statistic title="Job đang tuyển" value={overview?.openJobs || 0} formatter={formatter} />
                    </Card>
                </Col>
                <Col span={24} md={6}>
                    <Card hoverable>
                        <Statistic title="Tổng Jobs" value={overview?.jobsTotal || 0} formatter={formatter} />
                    </Card>
                </Col>
                <Col span={24} md={6}>
                    <Card hoverable>
                        <Statistic title="Tổng Resumes" value={overview?.resumesTotal || 0} formatter={formatter} />
                    </Card>
                </Col>
                <Col span={24} md={6}>
                    <Card hoverable>
                        <Statistic title="Tổng Users" value={overview?.usersTotal || 0} formatter={formatter} />
                    </Card>
                </Col>

                <Col span={24} md={12}>
                    <Card title="Resumes theo trạng thái" hoverable >
                        {(() => {
                            const raw: any[] = (overview?.resumesByStatus || []).filter((d: any) => d && (d.x !== undefined || d.name !== undefined));
                            const order = ['PENDING', 'REVIEWING', 'APPROVED', 'REJECTED'];
                            const labelMap: any = { PENDING: 'Chờ duyệt', REVIEWING: 'Đang xem', APPROVED: 'Đã duyệt', REJECTED: 'Từ chối' };
                            const colorMap: any = { PENDING: '#faad14', REVIEWING: '#1677ff', APPROVED: '#52c41a', REJECTED: '#ff4d4f' };
                            const total = raw.reduce((s, i) => s + (i?.y ?? i?.value ?? 0), 0);
                            if (!total) return <div style={{ textAlign: 'center', padding: 20, color: 'var(--sub-text)' }}>Không có dữ liệu</div>;
                            let data = raw
                                .map((d: any) => ({ name: labelMap[d.x ?? d.name] || (d.x ?? d.name), value: d.y ?? d.value ?? 0, key: d.x ?? d.name }))
                                .sort((a, b) => order.indexOf(a.key) - order.indexOf(b.key));
                            data = data.filter(d => statusFilter.includes(d.key));
                            if (statusChartType === 'pie') {
                                const domain = data.map(d => d.name);
                                const range = data.map(d => colorMap[d.key] || '#999');
                                return (
                                    <Pie
                                        data={data}
                                        angleField="value"
                                        colorField="name"
                                        radius={0.9}
                                        innerRadius={0.6}
                                        height={220}
                                        theme={plotTheme}
                                        legend={{ position: 'right', itemName: { style: { fill: 'var(--text)' } } }}
                                        scale={{ color: { domain, range } } as any}
                                        label={false}
                                    />
                                );
                            }
                            if (statusChartType === 'bar') {
                                const domain = data.map(d => d.name);
                                const range = data.map(d => colorMap[d.key] || '#999');
                                return (
                                    <Bar
                                        data={data}
                                        yField="name"
                                        xField="value"
                                        colorField="name"
                                        transpose
                                        scale={{ color: { domain, range } } as any}
                                        legend={{ position: 'right', itemName: { style: { fill: 'var(--text)' } } }}
                                        height={220}
                                        theme={plotTheme}
                                        xAxis={{ label: { style: { fill: 'var(--text)' } } }}
                                        yAxis={{ label: { style: { fill: 'var(--text)' } } }}
                                    />
                                );
                            }
                            return (
                                <Column
                                    data={data}
                                    xField="name"
                                    yField="value"
                                    colorField="name"
                                    scale={{ color: { domain: data.map(d => d.name), range: data.map(d => colorMap[d.key] || '#999') } } as any}
                                    legend={false}
                                    height={220}
                                    theme={plotTheme}
                                    xAxis={{ label: { style: { fill: 'var(--text)' } } }}
                                    yAxis={{ label: { style: { fill: 'var(--text)' } } }}
                                />
                            );
                        })()}
                    </Card>
                </Col>
                {
                    (roleName && (roleName.toUpperCase() === 'ADMIN' || roleName.toUpperCase() === 'SUPER_ADMIN')) && (
                        <Col span={24} md={12}>
                            <Card title="Top công ty đang tuyển" hoverable>
                                <List
                                    itemLayout="horizontal"
                                    dataSource={topCompanies}
                                    renderItem={(c: any) => (
                                        <List.Item>
                                            <List.Item.Meta
                                                avatar={<Avatar src={c.logo} />}
                                                title={<span style={{ overflow: 'hidden', whiteSpace: 'nowrap', textOverflow: 'ellipsis' }}>{c.name}</span>}
                                            />
                                            <div><b>{c.count}</b></div>
                                        </List.Item>
                                    )}
                                />
                            </Card>
                        </Col>
                    )
                }

                <Col span={24} md={12}>
                    <Card title="Jobs tạo theo ngày" hoverable >
                        {chartType === 'line' && <Line data={ts?.jobsPerDay || []} xField="x" yField="y" smooth point height={240} theme={plotTheme} />}
                        {chartType === 'area' && <Area data={ts?.jobsPerDay || []} xField="x" yField="y" line={{ smooth: true }} height={240} theme={plotTheme} />}
                        {chartType === 'column' && <Column data={ts?.jobsPerDay || []} xField="x" yField="y" height={240} theme={plotTheme} />}
                    </Card>
                </Col>
                <Col span={24} md={12}>
                    <Card title="Resumes tạo theo ngày" hoverable >
                        {chartType === 'line' && <Line data={ts?.resumesPerDay || []} xField="x" yField="y" smooth point height={240} theme={plotTheme} />}
                        {chartType === 'area' && <Area data={ts?.resumesPerDay || []} xField="x" yField="y" line={{ smooth: true }} height={240} theme={plotTheme} />}
                        {chartType === 'column' && <Column data={ts?.resumesPerDay || []} xField="x" yField="y" height={240} theme={plotTheme} />}
                        {/* stacked by status when available */}
                        {Array.isArray(ts?.resumesPerDayByStatus) && ts.resumesPerDayByStatus.length > 0 && (
                            <Area
                                data={ts.resumesPerDayByStatus}
                                xField="x"
                                yField="y"
                                seriesField="name"
                                colorField="name"
                                stack
                                height={240}
                                theme={plotTheme}
                            />
                        )}
                    </Card>
                </Col>
                <Col span={24} md={12}>
                    <Card title="Companies tạo theo ngày" hoverable >
                        {chartType === 'line' && <Line data={ts?.companiesPerDay || []} xField="x" yField="y" point height={240} theme={plotTheme} />}
                        {chartType === 'area' && <Area data={ts?.companiesPerDay || []} xField="x" yField="y" height={240} theme={plotTheme} />}
                        {chartType === 'column' && <Column data={ts?.companiesPerDay || []} xField="x" yField="y" height={240} theme={plotTheme} />}
                    </Card>
                </Col>
                <Col span={24} md={12}>
                    <Card title="Users tạo theo ngày" hoverable >
                        {chartType === 'line' && <Line data={ts?.usersPerDay || overview?.usersPerDay || []} xField="x" yField="y" point height={240} theme={plotTheme} />}
                        {chartType === 'area' && <Area data={ts?.usersPerDay || overview?.usersPerDay || []} xField="x" yField="y" height={240} theme={plotTheme} />}
                        {chartType === 'column' && <Column data={ts?.usersPerDay || overview?.usersPerDay || []} xField="x" yField="y" height={240} theme={plotTheme} />}
                    </Card>
                </Col>
            </Row >
        </div >
    )
}

export default DashboardPage;