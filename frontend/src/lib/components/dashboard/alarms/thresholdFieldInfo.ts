// This aims to be a recreation of AlarmThresholdFieldMetas from the backend

import type { ModelHeartbeat } from '$lib/api-client-axios';

export type ThresholdFieldMeta = {
	formattedName: string,
	unit: string,
	getValueFunc: (heartbeat: ModelHeartbeat) => number;
}

export const alarmThresholdFieldMetas: Record<number, ThresholdFieldMeta> = {
	0: {
		formattedName: "latency",
		unit: " ms",
		getValueFunc: heartbeat => heartbeat.latency!
	}
}

export const alarmThresholdFieldOptions = Object.entries(alarmThresholdFieldMetas).map(([key, value]) => {
	const formattedName = value.formattedName;
	return {value: key, label: formattedName.substring(0, 1).toUpperCase() + formattedName.substring(1)}
})