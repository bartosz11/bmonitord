<script lang="ts">
	import { additionalButtons, dashboardHeader } from '$lib/states';
	import { writable } from 'svelte/store';
	import DataTable from '$lib/components/dashboard/DataTable.svelte';
	import OrchestratorCreateDialog from '$lib/components/dashboard/admin/orchestrators/OrchestratorCreateDialog.svelte';
	import { orchestratorColumns } from '$lib/components/dashboard/admin/orchestrators/orchestratorColumns';

	let { data } = $props();

	dashboardHeader.set('Instance\'s orchestrators');
	additionalButtons.set(createButton);

	let orchestrators = writable(data.orchestrators);

</script>

{#snippet createButton()}
	<OrchestratorCreateDialog {orchestrators}/>
{/snippet}

<DataTable columns={orchestratorColumns} data={$orchestrators} meta={orchestrators} defaultSorting={{id: "id", desc: false}} />
