<script lang="ts">
	import Card from '$lib/components/ui/card/Card.svelte';
	import type { ModelUser } from '$lib/api-client-axios';
	import { cn } from '$lib/utils';

	let { user, class: className }: { user: ModelUser, class?: string } = $props();
</script>

<Card title="Account info" class={cn("md:min-w-72", className)}>
	{#snippet content()}
		<ul>
			<li>
				<span class="font-semibold">ID: </span>
				<span>{user.id}</span>
			</li>
			<li>
				<span class="font-semibold">Username: </span>
				<span>{user.username}</span>
			</li>
			<li>
				<span class="font-semibold">Administrator: </span>
				<span>{user.admin ? "yes" : "no"}</span>
			</li>
			<li>
				<span class="font-semibold">Creation date: </span>
				{#if user.createdAt}
					<span>{new Date(user.createdAt).toLocaleString(navigator.language)}</span>
				{:else}
					<span>unknown</span>
				{/if}
			</li>
			<li>
				<span class="font-semibold">Last update: </span>
				{#if user.updatedAt}
					<span>{new Date(user.updatedAt).toLocaleString(navigator.language)}</span>
				{:else}
					<span>unknown</span>
				{/if}
			</li>
		</ul>
	{/snippet}
</Card>
