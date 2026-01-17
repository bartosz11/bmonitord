<script lang="ts">
	import * as Tooltip from '$lib/components/ui/tooltip/index.js';
	import type { SvelteDate } from 'svelte/reactivity';
	import type { ModelIncident } from '$lib/api-client-axios';
	import { calculateTickUptime } from '$lib/components/report/uptimeUtil';

	type TimelineTickProps = {
		incidents: ModelIncident[]
		bucketStart: SvelteDate
		bucketEnd: SvelteDate
		index: number
	}

	let { incidents, bucketStart, bucketEnd }: TimelineTickProps = $props();

	let uptime = $derived(calculateTickUptime(incidents, bucketStart.getTime(), bucketEnd.getTime()));
	let tooltipContent = $derived(`${bucketStart.toLocaleString(navigator.language)} - ${bucketEnd.toLocaleString(navigator.language)} <br> Uptime: ${uptime.toFixed(3)}%`);
	let color = $derived(uptime === 100 ? 'bg-emerald-500'
		: uptime < 90 ? 'bg-red-500' : 'bg-amber-500');
</script>

<Tooltip.Root>
	<Tooltip.Trigger>
		{#snippet child({ props })}
			<div {...props} class={`w-2 h-5 rounded-md ${color}`}></div>
		{/snippet}
	</Tooltip.Trigger>

	<Tooltip.Content>
		<!-- eslint-disable-next-line svelte/no-at-html-tags This is also safe -->
		{@html tooltipContent}
	</Tooltip.Content>
</Tooltip.Root>
