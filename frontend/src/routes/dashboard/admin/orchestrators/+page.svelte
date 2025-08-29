<script lang="ts">
	import { additionalButtons, dashboardHeader } from '$lib/states';
	import { writable } from 'svelte/store';
	import DataTable from '$lib/components/dashboard/DataTable.svelte';
	import CreateDialog from '$lib/components/dashboard/admin/orchestrators/CreateDialog.svelte';
	import { orchestratorColumns } from '$lib/components/dashboard/admin/orchestrators/orchestratorColumns';

	let { data } = $props();

	dashboardHeader.set('Instance\'s orchestrators');
	additionalButtons.set(createButton);

	let orchestrators = writable(data.orchestrators);

</script>

{#snippet createButton()}
	<CreateDialog {orchestrators}/>
{/snippet}

<DataTable columns={orchestratorColumns} data={$orchestrators} meta={orchestrators} />
