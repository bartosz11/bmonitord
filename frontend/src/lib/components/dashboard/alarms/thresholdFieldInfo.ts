// This aims to be a recreation of AlarmThresholdFieldMetas from the backend

import type { ModelAlarm, ModelHeartbeat } from '$lib/api-client-axios';

export type ThresholdFieldMeta = {
	formattedName: string;
	unit: string;
	getValueFunc: (heartbeat: ModelHeartbeat, alarm: ModelAlarm) => number;
};

export const alarmThresholdFieldMetas: Record<number, ThresholdFieldMeta> = {
	0: {
		formattedName: 'latency',
		unit: ' ms',
		getValueFunc: (heartbeat) => heartbeat.latency!
	},
	1: {
		formattedName: 'CPU frequency',
		unit: ' Mhz',
		getValueFunc: (heartbeat) => {
			const data = heartbeat.payload!.data!;
			if ('cpu' in data) {
				const cpu = data.cpu as object;
				if ('frequency' in cpu) {
					return Number(cpu.frequency);
				}
			}
			return 0;
		}
	},
	2: {
		formattedName: 'CPU usage',
		unit: ' %',
		getValueFunc: (heartbeat) => {
			const data = heartbeat.payload!.data!;
			if ('cpu' in data) {
				const cpu = data.cpu as object;
				if ('usage' in cpu) {
					return Number(cpu.usage);
				}
			}
			return 0;
		}
	},
	3: {
		formattedName: 'IOWait',
		unit: ' %',
		getValueFunc: (heartbeat) => {
			const data = heartbeat.payload!.data!;
			if ('cpu' in data) {
				const cpu = data.cpu as object;
				if ('iowait' in cpu) {
					return Number(cpu.iowait);
				}
			}
			return 0;
		}
	},
	4: {
		formattedName: 'memory usage',
		unit: ' %',
		getValueFunc: (heartbeat) => {
			const data = heartbeat.payload!.data!;
			if ('memory' in data) {
				const memory = data.memory as object;
				calculateUsage(memory);
			}
			return 0;
		}
	},
	5: {
		formattedName: 'swap usage',
		unit: ' %',
		getValueFunc: (heartbeat) => {
			const data = heartbeat.payload!.data!;
			if ('swap' in data) {
				const swap = data.swap as object;
				calculateUsage(swap);
			}
			return 0;
		}
	},
	6: {
		formattedName: 'disk usage',
		unit: ' %',
		getValueFunc: (heartbeat, alarm) => {
			const data = heartbeat.payload!.data!;
			if ('disks' in data) {
				const disks = data.disks as object[];
				let usedSum = 0;
				let totalSum = 0;
				for (const disk of disks) {
					if (!('mount' in disk)) continue;
					if (disk.mount === alarm.thresholdFieldParams) {
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
	},
	7: {
		formattedName: 'NIC inbound traffic',
		unit: ' bytes',
		getValueFunc: (heartbeat, alarm) => {
			const data = heartbeat.payload!.data!;
			if ('network' in data) {
				const nics = data.network as object[];
				return getNICTraffic(nics, alarm, "rx");
			}
			return 0;
		}
	},
	8: {
		formattedName: 'NIC outbound traffic',
		unit: ' bytes',
		getValueFunc: (heartbeat, alarm) => {
			const data = heartbeat.payload!.data!;
			if ('network' in data) {
				const nics = data.network as object[];
				return getNICTraffic(nics, alarm, "tx");
			}
			return 0;
		}
	}
};

function getNICTraffic(nics: object[], alarm: ModelAlarm, field: string) {
	let trafficSum = 0;
	for (const nic of nics) {
		if (!("iface" in nic)) continue;
		// I love TS so much
		const castedNic  = nic as Record<string, string | number>;
		const nicTraffic = Number(castedNic[field]);

		if (nic.iface === alarm.thresholdFieldParams) return nicTraffic;
		trafficSum += nicTraffic;
	}

	return trafficSum;
}


function calculateUsage(info: object): number {
	if ('used' in info && 'total' in info) {
		return (Number(info.used) / Number(info.total)) * 100;
	}
	return 0;
}

export const alarmThresholdFieldOptions = Object.entries(alarmThresholdFieldMetas).map(([key, value]) => {
	const formattedName = value.formattedName + ` (${value.unit.trim()})`;
	return { value: key, label: formattedName.substring(0, 1).toUpperCase() + formattedName.substring(1) };
});
