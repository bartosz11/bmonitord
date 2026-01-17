import type { ModelIncident } from '$lib/api-client-axios';

export type Interval = {
	start: number; // ms timestamps
	end: number;
};

export function calculateTickUptime(incidents: ModelIncident[], tickStart: number, tickEnd: number): number {
	const overlaps: Interval[] = [];

	for (const incident of incidents) {
		const incidentStart = new Date(incident.start!).getTime();
		const incidentEnd = incident.ongoing ? tickEnd : new Date(incident.end!).getTime(); // ongoing → clamp to tick end
		// skip non-overlapping incidents
		if (incidentStart >= tickEnd || incidentEnd <= tickStart) {
			continue;
		}

		// Clamp to tick boundaries
		const start = Math.max(incidentStart, tickStart);
		const end = Math.min(incidentEnd, tickEnd);

		// should never happen but defensive programming, i guess
		if (start >= end) {
			continue;
		}

		overlaps.push({ start, end });
	}

	// No downtime → full uptime
	if (overlaps.length === 0) {
		return 100;
	}

	const merged = mergeIntervals(overlaps);

	let downtime = 0;
	for (const interval of merged) {
		downtime += interval.end - interval.start;
	}

	const duration = tickEnd - tickStart;

	const uptime = ((duration - downtime) / duration) * 100;

	// ticks should never exceed bounds, but once again defensive programming
	return Math.max(0, Math.min(100, uptime));
}

function mergeIntervals(intervals: Interval[]): Interval[] {
	if (intervals.length === 0) return [];

	intervals.sort((a, b) => a.start - b.start);

	const merged: Interval[] = [];
	let current = { ...intervals[0] };

	for (let i = 1; i < intervals.length; i++) {
		const next = intervals[i];

		if (next.start <= current.end) {
			current.end = Math.max(current.end, next.end);
		} else {
			merged.push(current);
			current = { ...next };
		}
	}

	merged.push(current);
	return merged;
}

