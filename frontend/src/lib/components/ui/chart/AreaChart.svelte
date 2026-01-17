<script lang="ts" module>
	export type AreaChartDataPoint = {
		x: Date;
		[key: string]: Date | number;
	};

	export type AreaChartSeriesConfig = {
		key: string,
		label?: string,
		color?: string,
		data?: AreaChartDataPoint[],
	}
</script>

<script lang="ts">
	import { AreaChart } from 'layerchart';
	import { scaleUtc } from 'd3-scale';
	import { ChartContainer, ChartTooltip, type ChartConfig } from '$lib/components/ui/chart/index';
	import { onMount } from 'svelte';

	type AreaChartProps = {
		x?: string,
		y?: string,
		axis?: boolean | 'x' | 'y',
		legend?: boolean,
		series: Array<AreaChartSeriesConfig>,
		data?: Array<unknown>,
		class?: string,
		brush?: boolean,
	}

	let {
		x = 'x',
		y = 'y',
		axis = true,
		legend = true,
		series,
		data,
		class: className,
		brush = true,
	}: AreaChartProps = $props();

	const mappedSeries = series.map(s => {
		return {
			key: s.key,
			label: s.label ?? s.key.substring(0, 1).toUpperCase() + s.key.substring(1).toLowerCase(),
			color: s.color ?? `var(--chart-${series.indexOf(s) + 1})`,
			data: s.data
		};
	});

	const mappedCfg = Object.fromEntries(
		mappedSeries.map((s) => [
			s.key,
			{ label: s.label, color: s.color }
		])
	) satisfies ChartConfig;


	// The purpose of this function is to prevent bs like "Mon Jan 01 2024 01:00:00 GMT+0100 (Central European Standard Time)" - it's "inconvenient"
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	function defaultDateFormatter(value: any) {
		// fallback
		if (!(value instanceof Date)) return `${value}`;
		return value.toLocaleString(navigator.language);
	}

	let container: HTMLElement;
	let width = $state(0);

	const ro = new ResizeObserver(([entry]) => {
		width = entry.contentRect.width;
	});

	onMount(() => {
		ro.observe(container);
		return () => ro.disconnect();
	});

	// For whatever reason the actual tick count differs from what this math provides / limits it to but as long as it's responsive and there's no overlap I don't really care
	const minTicks = 2;
	const maxTicks = 12;
	const pxPerTick = 140;

	function getTickCount(width: number) {
		return Math.min(
			maxTicks,
			Math.max(minTicks, Math.floor(width / pxPerTick))
		);
	}
</script>

<div bind:this={container}>
	<ChartContainer config={mappedCfg} class={className}>
		<AreaChart
			{x}
			{y}
			{axis}
			{legend}
			{data}
			{brush}

			xScale={scaleUtc()}
			series={mappedSeries}
			props={{
  	        xAxis: {
   					 ticks: getTickCount(width),
   					 format: (v) => v.toLocaleDateString(navigator.language),
	  				},
        }}
		>
			{#snippet tooltip()}
				<ChartTooltip labelFormatter={defaultDateFormatter} />
			{/snippet}
		</AreaChart>
	</ChartContainer>
</div>
