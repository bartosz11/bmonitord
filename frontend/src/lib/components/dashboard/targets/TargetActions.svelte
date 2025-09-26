<script lang="ts">
	import type { ModelTarget } from '$lib/api-client-axios';
	import type { Writable } from 'svelte/store';
	import ActionsBase from '$lib/components/dashboard/ActionsBase.svelte';
	import { DropdownMenuGroup } from '$lib/components/ui/dropdown-menu';
	import {
		DropdownMenuItem,
		DropdownMenuLabel,
		DropdownMenuSeparator
	} from '$lib/components/ui/dropdown-menu/index.js';
	import TargetDeleteDialog from '$lib/components/dashboard/targets/TargetDeleteDialog.svelte';
	import { targetApi } from '$lib/api';
	import { toast } from 'svelte-sonner';

	let { row, rows }: { row: ModelTarget, rows: Writable<ModelTarget[]> } = $props();

	function onPauseClick() {
		targetApi.targetTargetIDPausePatch(row.id!).then((resp) => {
			if (resp.status === 200) {
				toast.success(`Successfully ${row.paused ? "unpaused" : "paused"} monitoring.`);
				rows.update((arr) =>
					arr.map(n =>
						n.id! === row!.id! ? { ...n, ...resp.data.data! } : n
					));
			}
		}).catch((err) => toast.error('Failed to toggle pause status: ' + (err.response?.data?.error ?? 'something went wrong')))
	}
</script>

<!-- TODO: separate page for details/edit: add a query param like ?edit=true to automatically enable edit mode so we can have a "shortcut" to editing -->
<ActionsBase>
	{#snippet content()}
		<DropdownMenuGroup>
			<DropdownMenuLabel>Actions</DropdownMenuLabel>
			<DropdownMenuItem onclick={onPauseClick}>{row.paused ? "Unpause" : "Pause"} monitoring</DropdownMenuItem>
			<DropdownMenuItem>Details</DropdownMenuItem>
			<DropdownMenuItem>Edit</DropdownMenuItem>
			<DropdownMenuSeparator />
			<TargetDeleteDialog {row} {rows} />
		</DropdownMenuGroup>
	{/snippet}
</ActionsBase>