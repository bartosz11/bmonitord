<script lang="ts">
	import type { PageProps } from './$types';
	import Card from '$lib/components/ui/card/Card.svelte';
	import AreaChart from '$lib/components/ui/chart/AreaChart.svelte';
	import UptimeTimeline from '$lib/components/report/UptimeTimeline.svelte';
	import { isTargetTypePush, targetStatusAsText } from '$lib/components/dashboard/targets/utils';
	import dayjs from 'dayjs';
	import relativeTime from 'dayjs/plugin/relativeTime.js';
	import DataTable from '$lib/components/dashboard/DataTable.svelte';
	import { diskColumns, incidentColumns, perLocationColumns } from '$lib/components/report/reportColumns';
	import { type Chart, heartbeatsToGraphs } from '$lib/components/report/chartUtils';
	import { Select, SelectContent, SelectItem, SelectTrigger } from '$lib/components/ui/select';
	import { getTimestampFromRange, metricsRanges } from '$lib/components/report/timeUtils';
	import { heartbeatApi } from '$lib/api';
	import { toast } from 'svelte-sonner';
	import { getDisksInfo } from '$lib/components/heartbeatUtil';

	let { data }: PageProps = $props();
	dayjs.extend(relativeTime);

	const report = data.data;
	const name = report.target!.name!;
	const type = report.target!.type!;
	const lastStatus = report.target!.lastStatus!;
	const createdAt = new Date(report.target!.createdAt!);
	const createdAtFormatted = createdAt.toLocaleDateString(navigator.language);
	const uptime = report.uptime!;
	const lastCheckDetails = report.lastHeartbeatsFromLocations!;
	const incidentHistory = report.incidentHistory!;
	console.log(lastCheckDetails[0]);

	let metricsRange = $state('24h');
	const triggerContent = $derived(
		metricsRanges.find((i) => i.value === metricsRange)?.label ?? 'Select a time range'
	);

	let graphs: Chart[] = $state([]);
	let loadingMetrics = $state(false);

	async function loadMetricsGraphs() {
		if (!report.target) return;
		loadingMetrics = true;
		try {
			const startTimestamp = getTimestampFromRange(metricsRange, Math.floor(createdAt.getTime() / 1000));
			const resp = await heartbeatApi.heartbeatIdTimerangeGet(report.target.id!, startTimestamp);

			const hbs = resp.data.data ?? [];
			graphs = heartbeatsToGraphs(hbs, type);
		// Errors have to be of type any or unknown, both resulting in an ESLint error with the default SvelteKit config
		// eslint-disable-next-line @typescript-eslint/no-explicit-any
		} catch (err: any) {
			toast.error('Failed to fetch metrics: ' + (err.response?.data?.error ?? 'something went wrong'));
			graphs = [];
		} finally {
			loadingMetrics = false;
		}
	}

	$effect(() => {
		// eslint-disable-next-line @typescript-eslint/no-unused-expressions
		metricsRange; // dependency
		loadMetricsGraphs();
	});
</script>

<div class="w-full">
	<div class="m-8 flex flex-col gap-8">
		<div class="flex flex-col gap-6">
			<h1 class="font-bold text-4xl">{name}</h1>
			<div class="flex flex-col sm:flex-row gap-6">
				<Card class="sm:w-1/3" title="Current status">
					{#snippet content()}
						<p class="text-2xl font-semibold mb-2">{targetStatusAsText[lastStatus]}</p>
					{/snippet}
				</Card>
				<Card class="sm:w-1/3" title="Created">
					{#snippet content()}
						<span class="text-2xl font-semibold">{createdAtFormatted}</span>
					{/snippet}
				</Card>
				<Card class="sm:w-1/3" title="Overall uptime">
					{#snippet content()}
						<span class="text-2xl font-semibold">{uptime.overall}%</span>
					{/snippet}
				</Card>
			</div>
		</div>
		{#if !isTargetTypePush(type)}
			<div class="flex flex-col gap-4">
				<h2 class="text-3xl font-semibold">Last check</h2>
				<Card title="Per-location stats">
					{#snippet content()}
						<DataTable columns={perLocationColumns} data={lastCheckDetails}></DataTable>
					{/snippet}
				</Card>
			</div>
		{/if}
		<div class="flex flex-col gap-4">
			<h2 class="text-3xl font-semibold">Uptime</h2>
			<Card title="Uptime history">
				{#snippet content()}
					<UptimeTimeline pastDays={30} incidents={incidentHistory} />
				{/snippet}
			</Card>
			<div class="grid gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
				<Card title="Last 24 hours uptime:">
					{#snippet content()}
						<span class="text-2xl font-semibold">{uptime.last_24h}%</span>
					{/snippet}
				</Card>
				<Card title="Last 7 days uptime:">
					{#snippet content()}
						<span class="text-2xl font-semibold">{uptime.last_7d}%</span>
					{/snippet}
				</Card>
				<Card title="Last 30 days uptime:">
					{#snippet content()}
						<span class="text-2xl font-semibold">{uptime.last_30d}%</span>
					{/snippet}
				</Card>
				<Card title="Last 365 days uptime:">
					{#snippet content()}
						<span class="text-2xl font-semibold">{uptime.last_365d}%</span>
					{/snippet}
				</Card>
			</div>
		</div>
		<div class="flex flex-col gap-4">
			<h2 class="text-3xl font-semibold">Incident history</h2>
			<Card>
				{#snippet content()}
					<DataTable columns={incidentColumns} data={incidentHistory}
										 noResultsText="No incidents have been recorded in the last 30 days." />
				{/snippet}
			</Card>
		</div>

		<div class="flex flex-col gap-4">
			<div class="flex justify-between">
				<h2 class="text-3xl font-semibold">Metrics</h2>
				<Select type="single" bind:value={metricsRange}>
					<SelectTrigger class="w-40">
						{triggerContent}
					</SelectTrigger>
					<SelectContent>
						{#each metricsRanges as range (range.value)}
							<SelectItem label={range.label} value={range.value}>{range.label}</SelectItem>
						{/each}
					</SelectContent>
				</Select>
			</div>
			{#if loadingMetrics}
				<div class="text-muted-foreground text-sm">Loading metrics…</div>
			{:else}
				{#each graphs as graph (graph.cardTitle)}
					<Card title={`${graph.cardTitle} (${graph.unit})`}>
						{#snippet content()}
							<AreaChart series={graph.series} class="max-h-80 w-full" />
						{/snippet}
					</Card>
				{/each}
			{/if}
			{#if lastCheckDetails.length === 1 && type === 2}
				<Card title="Disk details">
					{#snippet content()}
						<DataTable columns={diskColumns} data={getDisksInfo(lastCheckDetails[0])}></DataTable>
					{/snippet}
				</Card>
			{/if}
		</div>
	</div>
</div>