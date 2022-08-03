import base64
import cpuinfo
import distro
import platform
import psutil
import requests
import time

# Don't change the version
VERSION = "1.0"
# Agent ID
SERVER = ""
# API URL, needs to end with /
URL = ""


def get_data():
    sec = int(time.strftime("%S"))
    sleep = 60 - sec
    if platform.system() == "Linux":
        os = base64.b64encode(f"{distro.name()} {distro.version()}".encode("utf-8")).decode("utf-8")
        iowait = psutil.cpu_times_percent(1).iowait
    else:
        os = base64.b64encode(f"{platform.system()} {platform.release()}".encode("utf-8")).decode("utf-8")
        # IOWait is not supported on Windows
        iowait = 0
    uptime = int(time.time() - psutil.boot_time())
    cpu_cores = int(psutil.cpu_count(False))
    cpu_freq = int(psutil.cpu_freq(False).current)
    cpu_model = base64.b64encode(cpuinfo.get_cpu_info()['brand_raw'].encode("utf-8")).decode("utf-8")
    netstats = psutil.net_io_counters()
    cpu_usage = psutil.cpu_percent(sleep)
    netstats2 = psutil.net_io_counters()
    ram_total = psutil.virtual_memory().total
    ram_usage = psutil.virtual_memory().percent
    swap_total = psutil.swap_memory().total
    swap_usage = psutil.swap_memory().percent
    rx = int((netstats2.bytes_recv - netstats.bytes_recv) / sleep)
    tx = int((netstats2.bytes_sent - netstats.bytes_sent) / sleep)
    disks = []
    disks_total_bytes = 0
    disks_used_bytes = 0
    for disk in psutil.disk_partitions():
        # Prevents checking for CD-ROMs and other similar devices
        if disk.fstype:
            disk_usage = psutil.disk_usage(disk.mountpoint).percent
            disk_total = psutil.disk_usage(disk.mountpoint).total
            disk_used = psutil.disk_usage(disk.mountpoint).used
            disk_data = f"{disk.mountpoint},{disk_usage},{disk_total},{disk_used}"
            disks.append(disk_data)
            disks_total_bytes += psutil.disk_usage(disk.mountpoint).total
            disks_used_bytes += psutil.disk_usage(disk.mountpoint).used
    # Decode at the end prevents the b prefix
    disks_encoded = base64.b64encode(';'.join(disks).encode("utf-8")).decode("utf-8")
    disks_total_percent = (disks_used_bytes / disks_total_bytes) * 100
    return f"{VERSION}|{SERVER}|{os}|{uptime}|{cpu_cores}|{cpu_freq}|{cpu_model}|{cpu_usage}|{ram_total}|{ram_usage}|{swap_total}|{swap_usage}|{iowait}|{rx}|{tx}|{disks_encoded}|{disks_total_percent}"


try:
    response = requests.post(URL, data=get_data(), timeout=15, verify=False)
except:
    pass
