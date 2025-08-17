import { LOCATION_LIST, SKILLS_LIST } from '@/config/utils';
import { EnvironmentOutlined, MonitorOutlined } from '@ant-design/icons';
import { ProForm } from '@ant-design/pro-components';
import { Button, Col, Form, Row, Select } from 'antd';

const SearchClient = ({ onSearch }: { onSearch?: (query: string) => void }) => {
    const optionsSkills = SKILLS_LIST;
    const optionsLocations = LOCATION_LIST;
    const [form] = Form.useForm();


    const onFinish = async (values: any) => {
        const skills: string[] = values?.skills ?? [];
        const locations: string[] = values?.locations ?? [];

        let query = '';
        if (skills.length) {
            const skillsParam = skills
                .map((s) => s.toLowerCase())
                .map((s) => `skills=${encodeURIComponent(s)}`)
                .join('&');
            query += skillsParam;
        }
        if (locations.length) {
            const locationsParam = locations
                .map((l) => l.toLowerCase())
                .map((l) => `locations=${encodeURIComponent(l)}`)
                .join('&');
            query += (query ? '&' : '') + locationsParam;
        }
        onSearch?.(query);
    }

    return (
        <ProForm
            form={form}
            onFinish={onFinish}
            submitter={
                {
                    render: () => <></>
                }
            }
        >
            <Row gutter={[20, 20]}>
                <Col span={24}><h2>Việc Làm IT Cho Developer "Chất"</h2></Col>
                <Col span={24} md={16}>
                    <ProForm.Item
                        name="skills"
                    >
                        <Select
                            mode="multiple"
                            allowClear
                            showArrow={false}
                            style={{ width: '100%' }}
                            placeholder={
                                <>
                                    <MonitorOutlined /> Tìm theo kỹ năng...
                                </>
                            }
                            optionLabelProp="label"
                            options={optionsSkills}
                        />
                    </ProForm.Item>
                </Col>
                <Col span={12} md={4}>
                    <ProForm.Item name="locations">
                        <Select
                            mode="multiple"
                            allowClear
                            showArrow={false}
                            style={{ width: '100%' }}
                            placeholder={
                                <>
                                    <EnvironmentOutlined /> Địa điểm...
                                </>
                            }
                            optionLabelProp="label"
                            options={optionsLocations}
                        />
                    </ProForm.Item>
                </Col>
                <Col span={12} md={4}>
                    <Button type='primary' onClick={() => form.submit()}>Search</Button>
                </Col>
            </Row>
        </ProForm>
    )
}
export default SearchClient;