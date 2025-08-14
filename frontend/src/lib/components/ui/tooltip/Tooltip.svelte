<script lang="ts">
	import * as Tooltip from '$lib/components/ui/tooltip/index.js';
	import type { Snippet } from 'svelte';

	let { content, trigger, ...rest }: TooltipProps = $props();

	type TooltipProps = {
		content: string;
		trigger: Snippet;
		[key: string]: unknown;
	};
</script>

<Tooltip.Provider>
	<Tooltip.Root>
		<Tooltip.Trigger {...rest}>{@render trigger()}</Tooltip.Trigger>
		<Tooltip.Content>
			{content}
		</Tooltip.Content>
	</Tooltip.Root>
</Tooltip.Provider>
