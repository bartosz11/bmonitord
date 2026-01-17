<script lang="ts">
	import * as Card from '$lib/components/ui/card/index.js';
	import type { Snippet } from 'svelte';

	let { title, description, action, content, footer, ...rest }: CardProps = $props();

	type CardProps = {
		title?: string | Snippet,
		description?: string,
		action?: Snippet,
		content: Snippet,
		footer?: Snippet,
		[key: string]: unknown,
	}
</script>

<Card.Root {...rest}>
	{#if title || description}
		<Card.Header>
			{#if title}
				<Card.Title>
					{#if typeof title === 'string'}
						{title}
					{:else}
						{@render title()}
					{/if}
				</Card.Title>
			{/if}
			{#if description}
				<Card.Description>{description}</Card.Description>
			{/if}
			{#if action}
				<Card.Action>
					{@render action()}
				</Card.Action>
			{/if}
		</Card.Header>
	{/if}
	<Card.Content>
		{@render content()}
	</Card.Content>
	{#if footer}
		<Card.Footer>
			{@render footer()}
		</Card.Footer>
	{/if}
</Card.Root>