<script lang="ts">
	import type { ModelNotification } from '$lib/api-client-axios';
	import { toast } from 'svelte-sonner';
	import type { Writable } from 'svelte/store';
	import ActionsBase from '$lib/components/dashboard/ActionsBase.svelte';
	import {
		DropdownMenuGroup,
		DropdownMenuItem,
		DropdownMenuLabel,
		DropdownMenuSeparator
	} from '$lib/components/ui/dropdown-menu';
	import { notificationApi } from '$lib/api';
	import NotificationDeleteDialog from '$lib/components/dashboard/notifications/NotificationDeleteDialog.svelte';
	import NotificationDataDialog from '$lib/components/dashboard/notifications/NotificationDataDialog.svelte';

	let { row, rows }: { row: ModelNotification, rows: Writable<ModelNotification[]> } = $props();

	function onSendTestClick() {
		notificationApi.notificationIdTestPost(row.id!).then((resp) => {
			if (resp.status === 204) {
				toast.success('A test notification has been sent successfully.');
			}
		}).catch((err) => toast.error('Failed to send test notification: ' + (err.response?.data?.error ?? 'something went wrong')));
	}
</script>

<ActionsBase>
	{#snippet content()}
		<DropdownMenuGroup>
			<DropdownMenuLabel>Actions</DropdownMenuLabel>
			<DropdownMenuItem onclick={onSendTestClick}>Send test notification</DropdownMenuItem>
			<NotificationDataDialog notifications={rows} notification={row}>
				{#snippet trigger()}
					<DropdownMenuItem onSelect={(e) => e.preventDefault()}>Edit</DropdownMenuItem>
				{/snippet}
			</NotificationDataDialog>
			<DropdownMenuSeparator />
			<NotificationDeleteDialog {row} {rows} />
		</DropdownMenuGroup>
	{/snippet}
</ActionsBase>