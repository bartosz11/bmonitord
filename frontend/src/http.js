import axios from "axios";
import { getCookie } from "svelte-cookie";

const http = axios.create({
    baseURL: import.meta.env.API_BASE_URL ?? 'http://localhost:8080',
    withCredentials: true,
    headers: {
        Authorization: `Bearer ${getCookie("auth-token")}`,
    }
})

export default http;