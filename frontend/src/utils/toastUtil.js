import toast from "svelte-french-toast";

const toastStyle = 'border-radius: 200px; background: #333; color: #fff;';

export function success(msg) {
    toast.success(msg, {
        style: toastStyle
    });
}

export function error(msg) { 
    toast.error(msg, {
        style: toastStyle
    });
}

export function info(msg, duration) { 
    toast(msg, {
        style: toastStyle,
        duration: duration
    });
}

export function promise(p, msgs, opts = {}) {
    // automatically hide toast if message is null
    const hide = { style: `display: none !important` };
    let options = { style: toastStyle };
    ["success", "error", "loading"].forEach((type) => {
        if (!msgs[type]) {
            options[type] = hide
        }
    })
    return toast.promise(p, msgs, { ...options, ...opts })
}
