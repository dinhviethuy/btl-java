import { Button, message } from "antd"
import axios from 'config/axios-customize';
import { useState } from "react"

function SendMail() {
  const [isLoading, setIsLoading] = useState(false)
  const handleSendMail = async () => {
    try {
      const access_token = localStorage.getItem('access_token')
      if (!access_token) {
        message.error('Chưa có access token')
        return
      }
      setIsLoading(true)
      const res = await axios.post('/api/v1/mail/send-mail', {
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          'Authorization': `Bearer ${access_token}`,
        },
      })
      if (res?.statusCode === 201) {
        message.success('Gửi mail thành công')
      } else {
        message.error('Gửi mail thất bại')
      }
    } catch (error) {
      message.error('Gửi mail thất bại')
    } finally {
      setIsLoading(false)
    }
  }
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
      <Button loading={isLoading} type="primary" onClick={handleSendMail} style={{ padding: '10px 20px', fontSize: '18px', width: '200px', height: '50px', fontWeight: 700 }}>
        Gửi mail
      </Button>
    </div>
  )
}

export default SendMail 