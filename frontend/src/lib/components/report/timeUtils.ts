import dayjs, { type ManipulateType } from 'dayjs';

// Literally the go HumanizeDuration function but in TS
export function humanizeDuration(durationNs: bigint): string {
	// Handle negative durations
	const d = durationNs < 0n ? -durationNs : durationNs;

	const totalSeconds = Number(d / 1_000_000_000n);
	const totalMinutes = Math.floor(totalSeconds / 60);
	const totalHours = Math.floor(totalMinutes / 60);

	const days = Math.floor(totalHours / 24);
	const hours = totalHours % 24;
	const minutes = totalMinutes % 60;
	const seconds = totalSeconds % 60;

	const parts: string[] = [];

	if (days > 0) {
		parts.push(plural(days, 'day'));
	}
	if (hours > 0) {
		parts.push(plural(hours, 'hour'));
	}
	if (minutes > 0) {
		parts.push(plural(minutes, 'minute'));
	}
	if (seconds > 0 || parts.length === 0) {
		parts.push(plural(seconds, 'second'));
	}

	return parts.join(', ');
}

function plural(value: number, unit: string): string {
	return value === 1 ? `${value} ${unit}` : `${value} ${unit}s`;
}

export type MetricsRange = {
	label: string,
	value: string,
	amount: number,
	unit: ManipulateType
}

export const metricsRanges: MetricsRange[] = [
	{ label: 'Last 3 hours', value: '3h', amount: 3, unit: 'hour' },
	{ label: 'Last 12 hours', value: '12h', amount: 12, unit: 'hour' },
	{ label: 'Last 24 hours', value: '24h', amount: 24, unit: 'hour' },
	{ label: 'Last 3 days', value: '3d', amount: 3, unit: 'day' },
	{ label: 'Last 7 days', value: '7d', amount: 7, unit: 'day' },
	{ label: 'Last 14 days', value: '14d', amount: 14, unit: 'day' },
	{ label: 'Last 30 days', value: '30d', amount: 30, unit: 'day' },
	{ label: 'Last 3 months', value: '3m', amount: 3, unit: 'month' },
	{ label: 'Last year', value: '1y', amount: 1, unit: 'year' },
	{ label: 'All time', value: 'all', amount: 0, unit: 'day' }
];

export function getTimestampFromRange(rangeValue: string, defaultTimestamp: number): number {
	const range = metricsRanges.find((r) => r.value === rangeValue);

	if (!range || range.value === 'all') {
		return defaultTimestamp;
	}

	return dayjs().subtract(range.amount, range.unit).unix();
}
