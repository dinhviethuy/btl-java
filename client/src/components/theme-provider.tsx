import { BulbOutlined } from "@ant-design/icons";
import { ConfigProvider, FloatButton, theme } from "antd";
import { useEffect, useMemo, useState } from "react";

export const ThemeProvider = ({ children }: { children: React.ReactNode }) => {
  const getInitial = () => {
    try {
      const stored = window.localStorage.getItem("theme");
      if (stored === "dark") return true;
      if (stored === "light") return false;
      // Mặc định LIGHT nếu chưa từng chọn
      return false;
    } catch {
      return false;
    }
  };

  const [isDarkMode, setIsDarkMode] = useState<boolean>(getInitial);

  useEffect(() => {
    const mode = isDarkMode ? "dark" : "light";
    try {
      window.localStorage.setItem("theme", mode);
    } catch { }
    // apply to document for CSS variables
    if (typeof document !== 'undefined') {
      document.documentElement.setAttribute('data-theme', mode);
    }
  }, [isDarkMode]);

  const toggleTheme = () => setIsDarkMode(prev => !prev);

  const algorithm = useMemo(() => (
    isDarkMode ? theme.darkAlgorithm : theme.defaultAlgorithm
  ), [isDarkMode]);

  const themeTokens = useMemo(() => ({
    token: {
      colorPrimary: '#4299e1',
      borderRadius: 8,
      borderRadiusLG: 12,
    }
  }), [isDarkMode]);
  return (
    <ConfigProvider theme={{ algorithm, ...themeTokens }}>
      {children}
      <FloatButton
        onClick={toggleTheme}
        type="default"
        tooltip={isDarkMode ? "Chế độ Sáng" : "Chế độ Tối"}
        icon={<BulbOutlined />}
      />
    </ConfigProvider>
  )
}