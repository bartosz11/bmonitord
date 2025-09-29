<script lang="ts">
	import { additionalButtons, dashboardHeader } from '$lib/states';
	import DataTable from '$lib/components/dashboard/DataTable.svelte';
	import { alarmColumns, type AlarmTableMeta } from '$lib/components/dashboard/alarms/alarmColumns';
	import { writable } from 'svelte/store';
	import AlarmDataDialog from '$lib/components/dashboard/alarms/AlarmDataDialog.svelte';
	import { Button } from '$lib/components/ui/button';

	let { data } = $props();

	dashboardHeader.set(`Alarms for ${data.target.name}`);
	additionalButtons.set(createDialog);

	let alarms = writable(data.alarms);
	const targetId = data.target.id!;

	const tableMeta: AlarmTableMeta = {
		alarms,
		notifications: data.notifications,
		target: data.target
	}
</script>

{#snippet createDialog()}
	<AlarmDataDialog {alarms} notifications={data.notifications} targetId={targetId}>
		{#snippet trigger()}
			<Button>Create alarm</Button>
		{/snippet}
	</AlarmDataDialog>
{/snippet}
<DataTable columns={alarmColumns} data={$alarms} meta={tableMeta} />