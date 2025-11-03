<script lang="ts">
	import { additionalButtons, dashboardHeader } from '$lib/states';
	import DataTable from '$lib/components/dashboard/DataTable.svelte';
	import { writable } from 'svelte/store';
	import type { ModelTarget } from '$lib/api-client-axios';
	import { targetColumns } from '$lib/components/dashboard/targets/targetColumns';
	import { Button } from '$lib/components/ui/button';

	let { data } = $props();

	dashboardHeader.set('Your targets');
	additionalButtons.set(createButton);

	const rows = writable<ModelTarget[]>(data.targets);

</script>

{#snippet createButton()}
	<Button href="/dashboard/targets/create">Create target</Button>
{/snippet}

<DataTable columns={targetColumns} data={$rows} meta={rows} defaultSorting={{id: "id", desc: false}}/>