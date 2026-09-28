import axios from "axios";
import {axiosEventSource} from "axios-eventsource";


// SSE 专用 axios 实例（绕过响应拦截器，直接处理流）
const sseClient = axios.create({
    baseURL: '/api',
    timeout: 120000,
    withCredentials: true
})

// 导出
export const chatApi = {

    // 发送请求，请求并处理SSE响应
    sendStream: (data, callbacks) => {
        // 解包回调函数
        const { onError, onStep, onDone, onResult } = callbacks || {}

        // 创建 SSE 请求
        const aes = axiosEventSource(sseClient, '/chat/send-stream', {
            method: 'POST',
            data: data,
            headers: {'Content-Type': 'application/json'},
            withCredentials: true,
            onerror: (e) => {
                onError?.(e)
            }
        })
        // 处理回调消息
        aes.onmessage = (event) => {
            try {
                const d = JSON.parse(event.data)
                if (d.type === 'step') {
                    onStep?.(d)
                } else if (d.type === 'done') {
                    onDone?.()
                    aes.close()
                } else {
                    // result: type 为 text/products/orders/navigation
                    onResult?.(d)
                }
            } catch { /* ignore parse errors */ }
        }
        return aes;
    }
}

