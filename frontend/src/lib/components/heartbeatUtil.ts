import type { ModelHeartbeat } from '$lib/api-client-axios';

export function getLatency(hb: ModelHeartbeat) {
	return hb.latency! ?? 0;
}

export function getCPUFrequency(heartbeat: ModelHeartbeat) {
	const data = heartbeat.payload!.data!;
	if ('cpu' in data) {
		const cpu = data.cpu as object;
		if ('frequency' in cpu) {
			return Number(cpu.frequency);
		}
	}
	return 0;
}

export function getCPUUsage(heartbeat: ModelHeartbeat) {
	const data = heartbeat.payload!.data!;
	if ('cpu' in data) {
		const cpu = data.cpu as object;
		if ('usage' in cpu) {
			return Number(cpu.usage);
		}
	}
	return 0;
}

export function getIOWait(heartbeat: ModelHeartbeat) {
	const data = heartbeat.payload!.data!;
	if ('cpu' in data) {
		const cpu = data.cpu as object;
		if ('iowait' in cpu) {
			return Number(cpu.iowait);
		}
	}
	return 0;
}

export function getMemoryUsage(heartbeat: ModelHeartbeat) {
	const data = heartbeat.payload!.data!;
	if ('memory' in data) {
		const memory = data.memory as object;
		return calculateUsage(memory);
	}
	return 0;
}

export function getSwapUsage(heartbeat: ModelHeartbeat) {
	const data = heartbeat.payload!.data!;
	if ('swap' in data) {
		const swap = data.swap as object;
		return calculateUsage(swap);
	}
	return 0;
}

export function getDiskUsage(heartbeat: ModelHeartbeat, mount?: string) {
	const data = heartbeat.payload!.data!;
	if ('disks' in data) {
		const disks = data.disks as object[];
		let usedSum = 0;
		let totalSum = 0;
		for (const disk of disks) {
			if (!('mount' in disk)) continue;
			if (disk.mount === mount) {
				return calculateUsage(disk);
			}

			if ('used' in disk && 'total' in disk) {
				usedSum += Number(disk.used);
				totalSum += Number(disk.total);
			}
		}
		return (usedSum / totalSum) * 100;
	}
	return 0;
}

export function getNICReceived(heartbeat: ModelHeartbeat, nic?: string) {
	const data = heartbeat.payload!.data!;
	if ('network' in data) {
		const nics = data.network as object[];
		return getNICTraffic(nics, 'rx', nic);
	}
	return 0;
}

export function getNICTransmitted(heartbeat: ModelHeartbeat, nic?: string) {
	const data = heartbeat.payload!.data!;
	if ('network' in data) {
		const nics = data.network as object[];
		return getNICTraffic(nics, 'tx', nic);
	}
	return 0;
}

export type DiskInfo = {
	mount: string,
	fs: string,
	used: number,
	total: number,
}

export function getDisksInfo(heartbeat: ModelHeartbeat): DiskInfo[] {
	const data = heartbeat.payload!.data!;
	console.log(data);
	const disksInfos: DiskInfo[] = [];
	if ('disks' in data) {
		const disks = data.disks as object[];
		for (const disk of disks) {
			if (!('mount' in disk) || !('fs' in disk) || !('used' in disk) || !('total' in disk)) continue;
			disksInfos.push({
				mount: String(disk.mount),
				fs: String(disk.fs),
				used: Number(disk.used),
				total: Number(disk.total)
			});
		}
	}
	return disksInfos;
}

function getNICTraffic(nics: object[], field: 'rx' | 'tx', nicName?: string) {
	let trafficSum = 0;
	for (const nic of nics) {
		if (!('iface' in nic)) continue;
		// I love TS so much
		const castedNic = nic as Record<string, string | number>;
		const nicTraffic = Number(castedNic[field]);

		if (nic.iface === nicName) return nicTraffic;
		trafficSum += nicTraffic;
	}

	return trafficSum;
}

function calculateUsage(info: object): number {
	if ('used' in info && 'total' in info) {
		const total = Number(info.total);
		if (total === 0) return 0;
		return (Number(info.used) / total) * 100;
	}
	return 0;
}
