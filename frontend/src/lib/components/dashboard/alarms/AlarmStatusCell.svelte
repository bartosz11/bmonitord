<script lang="ts">
	import type { ModelAlarm } from '$lib/api-client-axios';
	import { CirclePause, CircleCheck, CircleAlert } from '@lucide/svelte';
	import Tooltip from '$lib/components/ui/tooltip/Tooltip.svelte';

	let { alarm }: { alarm: ModelAlarm } = $props();

</script>

<Tooltip
	content={`${alarm.triggered ? 'Triggered' : 'Not triggered'}, ${alarm.suspended ? 'suspended' : 'not suspended'}, ${alarm.muted ? 'muted' : 'unmuted'} <br> Used retries: ${alarm.usedRetries} out of ${alarm.maxRetries}`}>
	{#snippet trigger()}
		{#if alarm.suspended}
			<CirclePause class="text-gray-500" />
		{:else if alarm.triggered}
				<CircleAlert class={alarm.muted ? "text-orange-500" : "text-red-500"} />
		{:else}
			<CircleCheck class="text-emerald-500" />
		{/if}
	{/snippet}
</Tooltip>