import axios from "axios";
import { getCookie } from "svelte-cookie";

const http = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL ?? "",
    withCredentials: true,
    headers: {
        Authorization: `Bearer ${getCookie("auth-token")}`,
    }
})

export default http;