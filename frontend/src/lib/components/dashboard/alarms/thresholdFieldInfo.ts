// This aims to be a recreation of AlarmThresholdFieldMetas from the backend

import type { ModelAlarm, ModelHeartbeat } from '$lib/api-client-axios';
import { getCPUFrequency, getCPUUsage, getDiskUsage, getIOWait, getLatency, getMemoryUsage, getNICReceived, getNICTransmitted, getSwapUsage } from '$lib/components/heartbeatUtil';

export type ThresholdFieldMeta = {
	formattedName: string;
	unit: string;
	getValueFunc: (heartbeat: ModelHeartbeat, alarm: ModelAlarm) => number;
};

export const alarmThresholdFieldMetas: Record<number, ThresholdFieldMeta> = {
	0: {
		formattedName: 'latency',
		unit: ' ms',
		getValueFunc: getLatency
	},
	1: {
		formattedName: 'CPU frequency',
		unit: ' MHz',
		getValueFunc: getCPUFrequency
	},
	2: {
		formattedName: 'CPU usage',
		unit: ' %',
		getValueFunc: getCPUUsage
	},
	3: {
		formattedName: 'IOWait',
		unit: ' %',
		getValueFunc: getIOWait
	},
	4: {
		formattedName: 'memory usage',
		unit: ' %',
		getValueFunc: getMemoryUsage
	},
	5: {
		formattedName: 'swap usage',
		unit: ' %',
		getValueFunc: getSwapUsage
	},
	6: {
		formattedName: 'disk usage',
		unit: ' %',
		getValueFunc: (heartbeat, alarm) => getDiskUsage(heartbeat, alarm.thresholdFieldParams)
	},
	7: {
		formattedName: 'NIC inbound traffic',
		unit: ' bytes',
		getValueFunc: (heartbeat, alarm) => getNICReceived(heartbeat, alarm.thresholdFieldParams)
	},
	8: {
		formattedName: 'NIC outbound traffic',
		unit: ' bytes',
		getValueFunc: (heartbeat, alarm) => getNICTransmitted(heartbeat, alarm.thresholdFieldParams)
	}
};

export const alarmThresholdFieldOptions = Object.entries(alarmThresholdFieldMetas).map(([key, value]) => {
	const formattedName = value.formattedName + ` (${value.unit.trim()})`;
	return { value: key, label: formattedName.substring(0, 1).toUpperCase() + formattedName.substring(1) };
});
