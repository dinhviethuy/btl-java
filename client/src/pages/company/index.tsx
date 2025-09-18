import CompanyCard from '@/components/client/card/company.card';
import { convertSlug } from '@/config/utils';
import { AutoComplete, Col, Input, Row } from 'antd';
import { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import styles from 'styles/client.module.scss';

const ClientCompanyPage = (props: any) => {
    const location = useLocation();
    const navigate = useNavigate();
    const [current, setCurrent] = useState<number>(1);
    const [pageSize, setPageSize] = useState<number>(8);
    const [search, setSearch] = useState<string>("");
    const [options, setOptions] = useState<{ value: string; label: JSX.Element }[]>([]);
    const [open, setOpen] = useState<boolean>(false);
    const inputRef = useRef<any>(null);

    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const c = parseInt(params.get('page') || '1');
        setCurrent(isNaN(c) ? 1 : c);
        // pageSize không lưu trên URL; giữ mặc định hiện tại
        const raw = params.get('name');
        const decoded = raw ? decodeURIComponent(raw) : "";
        const match = decoded.match(/^\/(.*)\/i$/);
        const kw = match ? match[1] : decoded;
        setSearch(kw);
        setOpen(false);
    }, [location.search]);

    const handlePageChange = (c: number, ps: number) => {
        const params = new URLSearchParams(location.search);
        params.set('page', c.toString());
        // không lưu pageSize lên URL
        navigate({ pathname: '/company', search: params.toString() }, { replace: true });
    };

    const pushUrl = (keyword: string) => {
        const params = new URLSearchParams(location.search);
        params.set('page', '1');
        if (keyword && keyword.trim()) params.set('name', encodeURIComponent('/' + keyword.trim() + '/i'));
        else params.delete('name');
        navigate({ pathname: '/company', search: params.toString() }, { replace: true });
    };

    const handleSearchTyping = async (value: string) => {
        setSearch(value);
        // Gợi ý tên công ty khi người dùng gõ
        if (!value || !value.trim()) {
            setOptions([]);
            setOpen(false);
            return;
        }
        try {
            const query = `current=1&pageSize=5&scope=public&name=${encodeURIComponent('/' + value.trim() + '/i')}`;
            const res = await import('@/config/api').then(m => m.callFetchCompany(query));
            if (res && res.data) {
                setOptions(res.data.result.map((c: any) => ({
                    value: `${c._id}::${c.name}`,
                    label: (
                        <div
                            style={{ display: 'flex', alignItems: 'center', gap: 8, width: '100%' }}
                            onMouseDown={(e) => e.preventDefault()}
                            onClick={() => {
                                setOpen(false);
                                const slug = convertSlug(c.name);
                                navigate({ pathname: `/company/${slug}`, search: `id=${c._id}` });
                            }}
                        >
                            <img src={c.logo} alt={c.name} style={{ width: 24, height: 24, objectFit: 'cover', borderRadius: 4 }} />
                            <span style={{ flex: 1 }}>{c.name}</span>
                        </div>
                    )
                })));
                setOpen(true);
            }
        } catch {
            setOptions([]);
            setOpen(false);
        }
    };

    return (
        <div className={styles["container"]} style={{ marginTop: 20 }}>
            <Row gutter={[20, 20]}>
                <Col span={24}>
                    <div style={{ marginBottom: 12 }}>
                        <AutoComplete
                            options={options}
                            value={search}
                            onSearch={handleSearchTyping}
                            onSelect={(val) => {
                                const [id, name] = String(val).split('::');
                                setSearch(name);
                                setOpen(false);
                                setOptions([]);
                                try { inputRef.current?.blur?.(); } catch { }
                                const slug = convertSlug(name);
                                navigate({ pathname: `/company/${slug}`, search: `id=${id}` });
                            }}
                            open={open && options.length > 0}
                            onDropdownVisibleChange={(v) => {
                                // Nếu đang có options và user đang click menu → giữ open tới khi select xử lý
                                if (!v && options.length > 0) return;
                                setOpen(v);
                            }}
                            getPopupContainer={(trigger) => trigger}
                            dropdownMatchSelectWidth={true}
                            dropdownStyle={{ zIndex: 2100 }}
                            dropdownRender={(menu) => (
                                <div onMouseDown={(e) => e.preventDefault()}>{menu}</div>
                            )}
                            style={{ width: '100%' }}
                        >
                            <Input.Search
                                placeholder="Tìm công ty..."
                                allowClear
                                onSearch={() => { try { inputRef.current?.blur?.(); } catch { }; pushUrl(search); }}
                                onChange={(e) => setSearch(e.target.value)}
                                ref={inputRef}
                            />
                        </AutoComplete>
                    </div>
                    <CompanyCard
                        showPagination={true}
                        defaultCurrent={current}
                        defaultPageSize={pageSize}
                        onPageChange={handlePageChange}
                        filter={new URLSearchParams(location.search).get('name') || ''}
                    />
                </Col>
            </Row>
        </div>
    )
}

export default ClientCompanyPage;