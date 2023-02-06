import toast from "svelte-french-toast";

export function success(msg) {
    toast.success(msg, {
        style: 'border-radius: 200px; background: #333; color: #fff;'
    });
}

export function error(msg) { 
    toast.error(msg, {
        style: 'border-radius: 200px; background: #333; color: #fff;'
    });
}

export function info(msg, duration) { 
    toast(msg, {
        style: 'border-radius: 200px; background: #333; color: #fff;'
        duration: duration
    });
}