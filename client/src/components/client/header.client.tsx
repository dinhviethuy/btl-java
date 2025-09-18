import { callLogout } from '@/config/api';
import { useAppDispatch, useAppSelector } from '@/redux/hooks';
import { setLogoutAction } from '@/redux/slice/accountSlide';
import styles from '@/styles/client.module.scss';
import { CodeOutlined, ContactsOutlined, DashOutlined, LogoutOutlined, MenuFoldOutlined, RiseOutlined, TwitterOutlined } from '@ant-design/icons';
import { Avatar, ConfigProvider, Drawer, Dropdown, Menu, MenuProps, Space, message } from 'antd';
import { useEffect, useState } from 'react';
import { isMobile } from 'react-device-detect';
import { FaReact } from 'react-icons/fa';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import ManageAccount from './modal/manage.account';

const Header = (props: any) => {
    const navigate = useNavigate();
    const dispatch = useAppDispatch();

    const isAuthenticated = useAppSelector(state => state.account.isAuthenticated);
    const user = useAppSelector(state => state.account.user);
    const [openMobileMenu, setOpenMobileMenu] = useState<boolean>(false);

    const [current, setCurrent] = useState('home');
    const location = useLocation();

    const [openMangeAccount, setOpenManageAccount] = useState<boolean>(false);

    useEffect(() => {
        setCurrent(location.pathname);
    }, [location])

    const items: MenuProps['items'] = [
        {
            label: <Link to={'/'}>Trang Chủ</Link>,
            key: '/',
            icon: <TwitterOutlined />,
        },
        {
            label: <Link to={'/job'}>Việc Làm IT</Link>,
            key: '/job',
            icon: <CodeOutlined />,
        },
        {
            label: <Link to={'/company'}>Top Công ty IT</Link>,
            key: '/company',
            icon: <RiseOutlined />,
        }
    ];



    const onClick: MenuProps['onClick'] = (e) => {
        setCurrent(e.key);
    };

    const handleLogout = async () => {
        const res = await callLogout();
        if (res && res.data) {
            dispatch(setLogoutAction({}));
            message.success('Đăng xuất thành công');
            navigate('/')
        }
    }

    const canSeeAdmin = isAuthenticated && user?.permissions && user.permissions.length > 0;
    const itemsDropdown: MenuProps['items'] = [
        {
            label: 'Quản lý tài khoản',
            key: 'manage-account',
            icon: <ContactsOutlined />
        },
        ...(canSeeAdmin ? [{
            label: 'Trang Quản Trị',
            key: 'admin',
            icon: <DashOutlined />
        }] : []),
        {
            label: 'Đăng xuất',
            key: 'logout',
            icon: <LogoutOutlined />
        },
    ];

    const handleDropdownClick: MenuProps['onClick'] = (info) => {
        switch (info.key) {
            case 'manage-account':
                setOpenManageAccount(true);
                break;
            case 'admin':
                navigate('/admin');
                break;
            case 'logout':
                handleLogout();
                break;
            default:
                break;
        }
    };

    const itemsMobiles = [...items, ...itemsDropdown];

    return (
        <>
            <div className={styles["header-section"]}>
                <div className={styles["container"]}>
                    {!isMobile ?
                        <div style={{ display: "flex", gap: 30 }}>
                            <div className={styles['brand']} >
                                <FaReact onClick={() => navigate('/')} title='Đinh Viết Huy' />
                            </div>
                            <div className={styles['top-menu']}>
                                <ConfigProvider
                                    theme={{
                                        token: {
                                            colorPrimary: '#4299e1',
                                            colorBgContainer: 'var(--header-bg)',
                                            colorText: 'var(--muted-text)',
                                        }
                                    }}
                                >

                                    <Menu
                                        // onClick={onClick}
                                        selectedKeys={[current]}
                                        mode="horizontal"
                                        items={items}
                                    />
                                </ConfigProvider>
                                <div className={styles['extra']}>
                                    {isAuthenticated === false ?
                                        <Link to={'/login'}>Đăng Nhập</Link>
                                        :
                                        <Dropdown menu={{ items: itemsDropdown, onClick: handleDropdownClick }} trigger={['click']}>
                                            <Space style={{ cursor: "pointer" }}>
                                                <span>Xin chào, {user?.name}</span>
                                                {user?.avatar ? (
                                                    <Avatar src={user.avatar} />
                                                ) : (
                                                    <Avatar>{user?.name?.substring(0, 2)?.toUpperCase()}</Avatar>
                                                )}
                                            </Space>
                                        </Dropdown>
                                    }

                                </div>

                            </div>
                        </div>
                        :
                        <div className={styles['header-mobile']}>
                            <span>Your APP</span>
                            <MenuFoldOutlined onClick={() => setOpenMobileMenu(true)} />
                        </div>
                    }
                </div>
            </div>
            <Drawer title="Chức năng"
                placement="right"
                onClose={() => setOpenMobileMenu(false)}
                open={openMobileMenu}
            >
                <Menu
                    onClick={onClick}
                    selectedKeys={[current]}
                    mode="vertical"
                    items={itemsMobiles}
                />
            </Drawer>
            <ManageAccount
                open={openMangeAccount}
                onClose={setOpenManageAccount}
            />
        </>
    )
};

export default Header;