<script lang="ts">
	import type { ModelTarget } from '$lib/api-client-axios';
	import { CircleArrowUp, CircleArrowDown, CircleHelp } from '@lucide/svelte';
	import Tooltip from '$lib/components/ui/tooltip/Tooltip.svelte';

	let { target }: { target: ModelTarget } = $props();

	const lastCheckDate = target.lastCheck!;
</script>

<Tooltip
	content={`Last check: ${new Date(lastCheckDate).toLocaleString()} <br> Used retries: ${target.usedRetries} out of ${target.maxRetries} <br> Paused: ${target.paused ? "yes" : "no"}`}>
	{#snippet trigger()}
		<!-- TODO: maybe fix up contrasts here later, on light theme they're an issue, on dark the gray isn't good enough too-->
		{#if target.lastStatus === 0}
			<CircleArrowUp class={target.paused ? "text-yellow-500" : "text-emerald-500"} />
		{:else if target.lastStatus === 1}
			<CircleArrowDown class={target.paused ? "text-yellow-500" : "text-red-500"} />
		{:else}
			<CircleHelp class={target.paused ? "text-yellow-500" : "text-gray-500"} />
		{/if}
	{/snippet}
</Tooltip>