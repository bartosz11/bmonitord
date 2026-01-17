import type { AreaChartSeriesConfig } from '$lib/components/ui/chart/AreaChart.svelte';
import type { ModelChecker, ModelHeartbeat, ModelTargetType } from '$lib/api-client-axios';
import { getCPUUsage, getDiskUsage, getIOWait, getLatency, getMemoryUsage, getNICReceived, getNICTransmitted, getSwapUsage } from '$lib/components/heartbeatUtil';

export type Chart = {
	cardTitle: string;
	unit: string;
	// Metrics that should appear on the chart
	metrics: Metric[];
	// the types of targets this chart should get rendered for
	types: ModelTargetType[];
	// series represent metrics that are meant to be displayed on the final chart but mapped to the series
	series: AreaChartSeriesConfig[];
};

type Metric = {
	key: string;
	label: string;
	getValue: (heartbeat: ModelHeartbeat) => number;
};

const METRICS = {
	latency: {
		key: 'latency',
		label: 'Latency',
		getValue: getLatency
	},
	cpuUsage: {
		key: 'cpuUsage',
		label: 'CPU usage',
		getValue: getCPUUsage
	},
	iowait: {
		key: 'iowait',
		label: 'IOWait',
		getValue: getIOWait
	},
	memoryUsage: {
		key: 'memoryUsage',
		label: 'Memory usage',
		getValue: getMemoryUsage
	},
	swapUsage: {
		key: 'swapUsage',
		label: 'Swap usage',
		getValue: getSwapUsage
	},
	diskUsage: {
		key: 'diskUsage',
		label: 'Disk usage',
		getValue: getDiskUsage
	},
	networkRx: {
		key: 'networkRx',
		label: 'Network received',
		getValue: (hb) => {
			return getNICReceived(hb) / 1000000;
		}
	},
	networkTx: {
		key: 'networkTx',
		label: 'Network transmitted',
		getValue: (hb) => {
			return getNICTransmitted(hb) / 1000000;
		}
	}
} as Record<string, Metric>;

const CHARTS: Chart[] = [
	{
		cardTitle: 'Latency',
		metrics: [METRICS.latency],
		types: [0, 1],
		series: [],
		unit: 'ms'
	},
	{
		cardTitle: 'CPU usage',
		metrics: [METRICS.cpuUsage, METRICS.iowait],
		types: [2],
		series: [],
		unit: '%'
	},
	{
		cardTitle: 'Memory usage',
		metrics: [METRICS.memoryUsage, METRICS.swapUsage],
		types: [2],
		series: [],
		unit: '%'
	},
	{
		cardTitle: 'Network usage',
		metrics: [METRICS.networkRx, METRICS.networkTx],
		types: [2],
		series: [],
		unit: 'Mbps'
	},
	{
		cardTitle: 'Disk usage',
		metrics: [METRICS.diskUsage],
		types: [2],
		series: [],
		unit: '%'
	}
];

export function heartbeatsToGraphs(heartbeats: ModelHeartbeat[], targetType: ModelTargetType): Chart[] {
	// group by locations and get a list of all locations
	const locations = new Map<number, ModelChecker>();
	const byLocation = new Map<number, ModelHeartbeat[]>();
	heartbeats.forEach((hb) => {
		const checkerId = hb.checkerId ?? 0;
		if (!byLocation.has(checkerId)) {
			byLocation.set(checkerId, []);
		}
		byLocation.get(checkerId)!.push(hb);
		if (hb.checker) {
			locations.set(checkerId, hb.checker);
		}
		const date = new Date(hb.timestamp!);
		date.setSeconds(0, 0);
		hb.timestamp = date.toISOString();
	});

	const charts = CHARTS.filter((chart) => chart.types.includes(targetType));
	for (const chart of charts) {
		for (const metric of chart.metrics) {
			for (const [checkerId, hbs] of byLocation) {
				const checker = locations.get(checkerId);
				chart.series.push({
					key: `${metric.key}-${checkerId}`,
					label: `${checker ? `${checker.name}${checker.location ? ' - ' : ''}${checker.location ?? ''} - ` : ''}${metric.label}`,
					data: hbs.map((hb) => {
						return {
							x: new Date(hb.timestamp!),
							y: metric.getValue(hb)
						};
					})
				});
			}
		}
	}

	return charts;
}
