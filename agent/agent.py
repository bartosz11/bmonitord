URL = "http://localhost:8080/orchestrator/agent/key"

import gzip
import io
import json
import platform
import time

import cpuinfo
import psutil
import requests
import urllib3


def collect_system_data():
    # Align the reports to next-minute mark
    sec = int(time.strftime("%S"))
    sleep = 60 - sec

    if platform.system() == "Linux":
        import distro
        os_name = f"{distro.name()} {distro.version()}"
    else:
        os_name = f"{platform.system()} {platform.release()}"

    # Unless we collect the metrics twice and sleep in between they're completely inaccurate
    cpu_start = psutil.cpu_times_percent(interval=None)
    net_start = psutil.net_io_counters(pernic=True)
    time.sleep(sleep)
    cpu_end = psutil.cpu_times_percent(interval=None)
    net_end = psutil.net_io_counters(pernic=True)

    iowait = getattr(cpu_end, "iowait", 0.0)
    loadavg = psutil.getloadavg()
    cpu_info = {
        "model": cpuinfo.get_cpu_info()['brand_raw'],
        "cores": psutil.cpu_count(logical=False) or 0,
        "threads": psutil.cpu_count(logical=True) or 0,
        "frequency": getattr(psutil.cpu_freq(False), "current", 0.0),
        "usage": round(100 - cpu_end.idle, 2),
        "iowait": round(iowait, 2),
        "loadavg": loadavg,
    }

    nics = []
    for name, stats in net_end.items():
        prev = net_start.get(name)
        if prev:
            nics.append({
                "iface": name,
                "rx": stats.bytes_recv - prev.bytes_recv,
                "tx": stats.bytes_sent - prev.bytes_sent,
            })

    mem = psutil.virtual_memory()
    swap = psutil.swap_memory()
    memory_info = {
        "total": mem.total,
        "used": mem.used,
    }
    swap_info = {
        "total": swap.total,
        "used": swap.used,
    }

    disks = []
    for part in psutil.disk_partitions(all=False):
        try:
            usage = psutil.disk_usage(part.mountpoint)
            disks.append({
                "mount": part.mountpoint,
                "fs": part.fstype,
                "total": usage.total,
                "used": usage.used,
            })
        except PermissionError:
            continue

    agent_data = {
        "version": "1.0.0",
        "hostname": platform.node(),
        "os": os_name,
        "kernel": platform.release(),
        "uptime": int(time.time() - psutil.boot_time()),
        "cpu": cpu_info,
        "memory": memory_info,
        "swap": swap_info,
        "disks": disks,
        "network": nics,
    }

    return {
        # 2 means type AGENT
        "type": 2,
        # v1 schema, the agent's version above can mean some minor updates
        "version": 1,
        "data": agent_data,
    }


def send_data(data):
    json_bytes = json.dumps(data).encode('utf-8')

    buf = io.BytesIO()
    with gzip.GzipFile(fileobj=buf, mode="wb") as gz:
        gz.write(json_bytes)

    compressed_data = buf.getvalue()

    # Send with appropriate headers
    headers = {
        "Content-Encoding": "gzip",
        "Content-Type": "application/json",
    }
    requests.post(URL, data=compressed_data, headers=headers, timeout=10, verify=False)


urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
collected_data = collect_system_data()
send_data(collected_data)
