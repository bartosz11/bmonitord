<script lang="ts">
	import AlertDialog from '$lib/components/ui/alert-dialog/AlertDialog.svelte';
	import type { ModelAlarm } from '$lib/api-client-axios';
	import { DropdownMenuItem } from '$lib/components/ui/dropdown-menu';
	import { toast } from 'svelte-sonner';
	import type { Writable } from 'svelte/store';
	import { alarmApi } from '$lib/api';

	let { row, rows }: { row: ModelAlarm, rows: Writable<ModelAlarm[]> } = $props();

	let open = $state(false);

	function onContinueClick() {
		alarmApi.targetTargetIDAlarmAlarmIDDelete(row.targetId!, row.id!).then((resp) => {
			if (resp.status === 204) {
				toast.success('Successfully deleted alarm.');
				rows.update((arr) => arr.filter((n) => n.id !== row.id!));
			}
		}).catch((err) => toast.error('Failed to delete alarm: ' + (err.response?.data?.error ?? 'something went wrong')))
			.finally(() => open = false);
	}
</script>

<AlertDialog title={`Are you sure you want to delete ${row.name}?`}
						 description="This action is irreversible!"
						 continueOnClick={onContinueClick} bind:open triggerContainerClass="w-full">
	{#snippet trigger()}
		<DropdownMenuItem class="text-destructive" onSelect={(e) => e.preventDefault()}>Delete</DropdownMenuItem>
	{/snippet}
</AlertDialog>
