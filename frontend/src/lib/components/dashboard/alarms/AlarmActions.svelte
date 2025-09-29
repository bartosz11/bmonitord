<script lang="ts">

	import type { ModelAlarm, ModelNotification, ModelTarget } from '$lib/api-client-axios';
	import type { Writable } from 'svelte/store';
	import { toast } from 'svelte-sonner';
	import ActionsBase from '$lib/components/dashboard/ActionsBase.svelte';
	import {
		DropdownMenuGroup,
		DropdownMenuItem,
		DropdownMenuLabel,
		DropdownMenuSeparator
	} from '$lib/components/ui/dropdown-menu';
	import AlarmDataDialog from '$lib/components/dashboard/alarms/AlarmDataDialog.svelte';
	import { alarmApi } from '$lib/api';
	import AlarmDeleteDialog from '$lib/components/dashboard/alarms/AlarmDeleteDialog.svelte';

	let { row, rows, target, notifications }: {
		row: ModelAlarm,
		rows: Writable<ModelAlarm[]>,
		target: ModelTarget,
		notifications: ModelNotification[]
	} = $props();

	const targetId = target.id!;

	function onMuteToggleClick() {
		alarmApi.targetTargetIDAlarmAlarmIDMutePatch(targetId, row.id!).then((resp) => {
			if (resp.status === 200) {
				toast.success(`Successfully ${row.muted ? "unmuted" : "muted"} alarm.`);
				rows.update((arr) =>
					arr.map(n =>
						n.id! === row!.id! ? { ...n, ...resp.data.data! } : n
					));
			}
		}).catch((err) => toast.error('Failed to toggle mute status: ' + (err.response?.data?.error ?? 'something went wrong')))
	}
</script>

<ActionsBase>
	{#snippet content()}
		<DropdownMenuGroup>
			<DropdownMenuLabel>Actions</DropdownMenuLabel>
			<DropdownMenuItem onclick={onMuteToggleClick}>{row.muted ? "Unmute" : "Mute"}</DropdownMenuItem>
			<AlarmDataDialog {targetId} alarm={row} {notifications} alarms={rows}>
				{#snippet trigger()}
					<DropdownMenuItem onclick={(e) => e.preventDefault()}>Edit</DropdownMenuItem>
				{/snippet}
			</AlarmDataDialog>
			<DropdownMenuSeparator />
			<AlarmDeleteDialog {row} {rows} />
		</DropdownMenuGroup>
	{/snippet}
</ActionsBase>