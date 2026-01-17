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

<Tooltip.Root>
	<Tooltip.Trigger {...rest}>{@render trigger()}</Tooltip.Trigger>
	<Tooltip.Content>
		<!-- eslint-disable-next-line svelte/no-at-html-tags This is safe -->
		{@html content}
	</Tooltip.Content>
</Tooltip.Root>
