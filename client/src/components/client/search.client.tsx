import { callFetchCompany, callFetchCompanyById } from '@/config/api';
import { LOCATION_LIST, SKILLS_LIST } from '@/config/utils';
import { CheckOutlined, EnvironmentOutlined, MonitorOutlined } from '@ant-design/icons';
import { ProForm } from '@ant-design/pro-components';
import { AutoComplete, Button, Col, Form, Row, Select, Tag } from 'antd';
import { useEffect, useRef, useState } from 'react';
import { useLocation } from 'react-router-dom';

const SearchClient = ({ onSearch }: { onSearch?: (query: string) => void }) => {
    const optionsSkills = SKILLS_LIST;
    const optionsLocations = LOCATION_LIST;
    const [form] = Form.useForm();
    const locationsWatch = Form.useWatch('locations', form) as string[];
    const location = useLocation();
    const [companyOptions, setCompanyOptions] = useState<{ label: string, value: string }[]>([]);
    const [companyInput, setCompanyInput] = useState<string>("");
    const [selectedCompanyId, setSelectedCompanyId] = useState<string | undefined>(undefined);
    const [selectedCompanyName, setSelectedCompanyName] = useState<string | undefined>(undefined);
    const debounceRef = useRef<any>();
    const [selectedSkills, setSelectedSkills] = useState<string[]>([]);


    const onFinish = async (values: any) => {
        const skills: string[] = selectedSkills ?? [];
        const locations: string[] = values?.locations ?? [];
        const companyId: string | undefined = selectedCompanyId || values?.companyId;
        const name: string | undefined = values?.name || (selectedCompanyId ? undefined : (companyInput?.trim() || undefined));

        let query = '';
        if (skills.length) {
            const skillsParam = skills
                .map((s) => `skills=${encodeURIComponent(s)}`)
                .join('&');
            query += skillsParam;
        }
        if (locations.length) {
            const locationsParam = locations
                .map((l) => `locations=${encodeURIComponent(l)}`)
                .join('&');
            query += (query ? '&' : '') + locationsParam;
        }
        if (companyId) {
            query += (query ? '&' : '') + `companyId=${encodeURIComponent(companyId)}`;
        }
        if (name) {
            query += (query ? '&' : '') + `name=${encodeURIComponent(name)}`;
        }
        onSearch?.(query);
    }

    // Hydrate form from URL on mount and when URL changes
    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const urlSkills = params.getAll('skills').map((s) => s.toUpperCase());
        const urlLocations = params.getAll('locations').map((l) => l.toUpperCase());
        const urlCompanyId = params.get('companyId') || undefined;
        const urlName = params.get('name') || '';
        const values: any = {};
        if (urlLocations.length) values.locations = urlLocations;
        if (urlCompanyId) values.companyId = urlCompanyId;
        if (urlName) { values.name = urlName; setCompanyInput(urlName); }
        if (urlCompanyId) {
            setSelectedCompanyId(urlCompanyId);
            // fetch company name to show chip label
            (async () => {
                try {
                    const res = await callFetchCompanyById(urlCompanyId);
                    if (res?.data?.name) setSelectedCompanyName(res.data.name);
                } catch { /* ignore */ }
            })();
        }
        setSelectedSkills(urlSkills);
        if (Object.keys(values).length) form.setFieldsValue(values);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [location.search]);

    // Remote suggestions for company by name (debounced)
    useEffect(() => {
        if (debounceRef.current) clearTimeout(debounceRef.current);
        debounceRef.current = setTimeout(async () => {
            const keyword = companyInput?.trim();
            if (!keyword) {
                setCompanyOptions([]);
                return;
            }
            const res = await callFetchCompany(`current=1&pageSize=8&scope=public&name=${encodeURIComponent(keyword)}`);
            if (res?.data?.result) {
                const opts = res.data.result
                    .filter((c) => Boolean(c.name) && Boolean(c._id))
                    .map((c) => ({ label: String(c.name), value: String(c._id), logo: c.logo }))
                setCompanyOptions(opts);
            } else {
                setCompanyOptions([]);
            }
        }, 300);
        return () => clearTimeout(debounceRef.current);
    }, [companyInput]);

    return (
        <ProForm
            form={form}
            onFinish={onFinish}
            submitter={{ render: () => <></> }}
        >
            <Row gutter={[12, 12]} wrap align="top">
                <Col span={24}><h2 style={{ fontSize: 34, fontWeight: 800, marginBottom: 8 }}>Việc Làm IT Cho Developer "Chất"</h2></Col>
                <Col span={24} style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    {/* hidden field for selected companyId */}
                    <ProForm.Item name="companyId" style={{ display: 'none' }}>
                        <input />
                    </ProForm.Item>
                    <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                        <ProForm.Item name="name" style={{ marginBottom: 0 }}>
                            <AutoComplete
                                style={{ width: '100%' }}
                                allowClear
                                dropdownMatchSelectWidth={420}
                                options={[
                                    ...((companyInput ? optionsSkills.filter(s => s.label.toLowerCase().includes(companyInput.toLowerCase()) || s.value.toLowerCase().includes(companyInput.toLowerCase())) : optionsSkills)
                                        .slice(0, 6)
                                        .map(s => {
                                            const active = selectedSkills.includes(s.value);
                                            return ({
                                                value: s.label,
                                                label: (
                                                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                                        <span style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', width: 20, height: 20, borderRadius: 4, background: 'rgba(250, 173, 20, 0.2)', color: '#faad14', fontWeight: 600 }}>#</span>
                                                        <span style={{ fontWeight: active ? 700 : 400 }}>{s.label}</span>
                                                        <span style={{ marginLeft: 'auto', color: active ? '#1677ff' : 'var(--muted-text)', fontSize: 12 }}>{active ? 'Selected' : 'Tag'}</span>
                                                    </div>
                                                ),
                                                metaType: 'skill',
                                                metaValue: s.value
                                            })
                                        })),
                                    ...companyOptions.map((o: { label: string; value: string; logo?: string }) => {
                                        const active = selectedCompanyId === o.value;
                                        return ({
                                            value: o.label,
                                            label: (
                                                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                                                    {o.logo && <img src={o.logo} alt={o.label} style={{ width: 22, height: 22, objectFit: 'contain', borderRadius: 4 }} />}
                                                    <span style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', fontWeight: active ? 700 : 400 }}>{o.label}</span>
                                                    <span style={{ color: active ? '#1677ff' : 'var(--muted-text)', fontSize: 12 }}>{active ? 'Selected' : 'Company'}</span>
                                                </div>
                                            ),
                                            metaType: 'company',
                                            metaValue: o.value
                                        })
                                    })
                                ]}
                                onSelect={(value, option: any) => {
                                    if (option?.metaType === 'skill') {
                                        const code = String(option.metaValue);
                                        setSelectedSkills(prev => {
                                            const exists = prev.includes(code);
                                            return exists ? prev.filter(x => x !== code) : Array.from(new Set([...prev, code]));
                                        });
                                        // clear input so không hiển thị lại text vừa chọn
                                        form.setFieldsValue({ name: '' });
                                        setCompanyInput('');
                                    } else if (option?.metaType === 'company') {
                                        const id = String(option.metaValue);
                                        const name = String(option.value);
                                        setSelectedCompanyId(id);
                                        setSelectedCompanyName(name);
                                        form.setFieldsValue({ companyId: id });
                                        // không thay đổi input keyword
                                    }
                                }}
                                onSearch={(value) => setCompanyInput(value)}
                                onChange={(value) => { setCompanyInput(value); setSelectedCompanyId(undefined); setSelectedCompanyName(undefined); form.setFieldsValue({ companyId: undefined }); }}
                                placeholder={<><MonitorOutlined /> Tìm theo từ khóa (kỹ năng, công ty, tên job)...</>}
                                filterOption={(input, option) => (option?.value as string)?.toLowerCase().includes(input.toLowerCase())}
                            />
                        </ProForm.Item>
                        {/* chips moved below to keep inputs aligned */}
                    </div>
                    {/* <ProForm.Item name="locations" tooltip={locationsWatch && locationsWatch.length ? locationsWatch.join(', ') : undefined} style={{ marginBottom: 0, width: 360 }}>
                        <Select
                            mode="multiple"
                            allowClear
                            showArrow={false}
                            style={{ width: '100%' }}
                            maxTagCount={3}
                            maxTagTextLength={12}
                            placeholder={<><EnvironmentOutlined /> Địa điểm...</>}
                            optionLabelProp="label"
                            optionFilterProp="label"
                            options={optionsLocations}
                            maxTagPlaceholder={(omittedValues: any[]) => {
                                const text = omittedValues.map((o: any) => {
                                    const rawVal = (o && typeof o === 'object' && 'value' in o) ? (o as any).value : o;
                                    const rawLabel = (o && typeof o === 'object' && 'label' in o) ? (o as any).label : undefined;
                                    if (typeof rawLabel === 'string') return rawLabel;
                                    const match = optionsLocations.find(opt => String(opt.value) === String(rawVal));
                                    return match?.label ?? String(rawVal);
                                }).join(', ');
                                return <span title={text}>+{omittedValues?.length}</span>;
                            }}
                            tagRender={(tagProps) => {
                                const { label, value, closable, onClose } = tagProps as any;
                                const realValue = (value && typeof value === 'object' && 'value' in value) ? value.value : value;
                                let displayLabel: string;
                                if (typeof label === 'string') displayLabel = label;
                                else {
                                    const match = optionsLocations.find(opt => String(opt.value) === String(realValue));
                                    displayLabel = match?.label ?? String(realValue ?? '');
                                }
                                return (
                                    <span onMouseDown={(e) => e.preventDefault()} style={{ display: 'inline-flex', alignItems: 'center' }} title={String(displayLabel)}>
                                        <Tag
                                            color="blue"
                                            closable={closable}
                                            onClose={onClose}
                                            style={{ marginInlineEnd: 4 }}
                                        >
                                            {displayLabel}
                                        </Tag>
                                    </span>
                                );
                            }}
                        />
                    </ProForm.Item> */}
                    <ProForm.Item
                        name="locations"
                        tooltip={
                            locationsWatch && locationsWatch.length
                                ? locationsWatch.join(', ')
                                : undefined
                        }
                        style={{ marginBottom: 0, width: 360 }}
                    >
                        <Select
                            mode="multiple"
                            allowClear
                            showArrow={false}
                            style={{ width: '100%' }}
                            maxTagCount={3}
                            maxTagTextLength={12}
                            placeholder={
                                <>
                                    <EnvironmentOutlined /> Địa điểm...
                                </>
                            }
                            optionLabelProp="label"
                            optionFilterProp="label"
                            options={optionsLocations}
                            // 👉 custom hiển thị omitted values
                            maxTagPlaceholder={(omittedValues) => {
                                const labels = omittedValues.map((o) => o.label ?? o.value);
                                return (
                                    <span title={labels.join(', ')}>
                                        +{omittedValues.length}
                                    </span>
                                );
                            }}
                            tagRender={(tagProps) => {
                                const { label, closable, onClose } = tagProps as any;
                                return (
                                    <Tag
                                        color="blue"
                                        closable={closable}
                                        onClose={onClose}
                                        style={{ marginInlineEnd: 4 }}
                                    >
                                        {label}
                                    </Tag>
                                );
                            }}
                        />
                    </ProForm.Item>

                    <Button type='primary' onClick={() => form.submit()} style={{ whiteSpace: 'nowrap' }}>Tìm kiếm</Button>
                </Col>
            </Row>
            {(selectedCompanyId || selectedSkills.length > 0) && (
                <div style={{ marginTop: 8, display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                    {selectedCompanyId && (
                        <Tag
                            color="blue"
                            closable
                            style={{ fontWeight: 700 }}
                            onClose={(e) => { e.preventDefault(); setSelectedCompanyId(undefined); setSelectedCompanyName(undefined); form.setFieldsValue({ companyId: undefined }); }}
                            icon={<CheckOutlined />}
                        >
                            {selectedCompanyName || 'Company selected'}
                        </Tag>
                    )}
                    {selectedSkills.map(s => (
                        <Tag key={s} color="gold" style={{ fontWeight: 700 }} closable onClose={(e) => { e.preventDefault(); setSelectedSkills(prev => prev.filter(x => x !== s)); }}>
                            #{optionsSkills.find(o => o.value === s)?.label || s}
                        </Tag>
                    ))}
                </div>
            )}
        </ProForm>
    )
}
export default SearchClient;