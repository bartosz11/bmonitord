<script lang="ts">
	import { additionalButtons, dashboardHeader } from '$lib/states';
	import DataTable from '$lib/components/dashboard/DataTable.svelte';
	import { columns } from '$lib/components/dashboard/admin/checkers/columns';
	import CreateDialog from '$lib/components/dashboard/admin/checkers/CreateDialog.svelte';
	import { writable } from 'svelte/store';

	let { data } = $props();

	let checkers = writable(data.checkers);
	dashboardHeader.set('Instance\'s checkers');
	additionalButtons.set(createButton);
</script>

{#snippet createButton()}
	<CreateDialog {checkers}/>
{/snippet}

<div class="flex flex-col gap-4">
	<DataTable data={$checkers} columns={columns} meta={checkers}/>
</div>

