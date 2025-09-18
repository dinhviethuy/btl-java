
const Footer = () => {
    return (
        <footer style={{
            padding: 20,
            textAlign: "center",
            background: "var(--background)",
            color: "var(--text)",
            borderTop: "1px solid var(--border)",
            marginTop: 40
        }}>
            <div style={{ marginBottom: 8 }}>
                <strong>Job Portal - Tìm việc IT chất lượng</strong>
            </div>
            <div style={{ fontSize: 14, color: "var(--muted-text)" }}>
                Kết nối nhà tuyển dụng và ứng viên IT | Email: contact@jobportal.vn | Hotline: 1900-xxxx
            </div>
            <div style={{ marginTop: 8 }}>
                Copyright &copy; 2025 Đinh Viết Huy. All rights reserved.
            </div>
        </footer>
    )
}

export default Footer;