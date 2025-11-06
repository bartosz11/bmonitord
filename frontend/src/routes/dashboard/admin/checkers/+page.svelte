<script lang="ts">
	import { additionalButtons, dashboardHeader } from '$lib/states';
	import DataTable from '$lib/components/dashboard/DataTable.svelte';
	import { checkerColumns } from '$lib/components/dashboard/admin/checkers/checkerColumns';
	import CheckerCreateDialog from '$lib/components/dashboard/admin/checkers/CheckerCreateDialog.svelte';
	import { writable } from 'svelte/store';

	let { data } = $props();

	let checkers = writable(data.checkers);
	dashboardHeader.set('Instance\'s checkers');
	additionalButtons.set(createButton);
</script>

{#snippet createButton()}
	<CheckerCreateDialog {checkers} />
{/snippet}

<DataTable data={$checkers} columns={checkerColumns} meta={checkers} defaultSorting={{id: "id", desc: false}}/>

