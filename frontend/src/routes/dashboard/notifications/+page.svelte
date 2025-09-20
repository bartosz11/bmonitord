<script lang="ts">
	import { additionalButtons, dashboardHeader } from '$lib/states';
	import DataTable from '$lib/components/dashboard/DataTable.svelte';
	import { notificationColumns } from '$lib/components/dashboard/notifications/notificationColumns';
	import { writable } from 'svelte/store';
	import NotificationDataDialog from '$lib/components/dashboard/notifications/NotificationDataDialog.svelte';
	import { Button } from '$lib/components/ui/button';
	import { subFormOutput } from '$lib/components/dashboard/notifications/utils';

	let { data } = $props();

	let notifications = writable(data.notifications);

	dashboardHeader.set('Your notifications');
	additionalButtons.set(createButton);
</script>

{#snippet createButton()}
	<NotificationDataDialog {notifications}>
		{#snippet trigger()}
			<Button onclick={() => subFormOutput.set("")}>
				Create notification
			</Button>
		{/snippet}
	</NotificationDataDialog>
{/snippet}

<DataTable columns={notificationColumns} data={$notifications} meta={notifications} />