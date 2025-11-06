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
	import { goto } from '$app/navigation';
	import AgentKeyRegenDialog from '$lib/components/dashboard/targets/agent/AgentKeyRegenDialog.svelte';
	import AgentSetupInstructions from '$lib/components/dashboard/targets/agent/AgentSetupInstructions.svelte';

	let { row, rows }: { row: ModelTarget, rows: Writable<ModelTarget[]> } = $props();

	function onPauseClick() {
		targetApi.targetTargetIDPausePatch(row.id!).then((resp) => {
			if (resp.status === 200) {
				toast.success(`Successfully ${row.paused ? 'unpaused' : 'paused'} monitoring.`);
				rows.update((arr) =>
					arr.map(n =>
						n.id! === row!.id! ? { ...n, ...resp.data.data! } : n
					));
			}
		}).catch((err) => toast.error('Failed to toggle pause status: ' + (err.response?.data?.error ?? 'something went wrong')));
	}
</script>

<ActionsBase>
	{#snippet content()}
		<DropdownMenuGroup>
			<DropdownMenuLabel>Actions</DropdownMenuLabel>
			<DropdownMenuItem onclick={onPauseClick}>{row.paused ? "Unpause" : "Pause"} monitoring</DropdownMenuItem>
			<DropdownMenuItem onclick={() => goto(`/dashboard/targets/${row.id}/edit`)}>Edit</DropdownMenuItem>
			<DropdownMenuItem onclick={() => goto(`/dashboard/targets/${row.id}/alarms`)}>Manage alarms</DropdownMenuItem>
			{#if row.type === 2}
				<DropdownMenuSeparator />
				<AgentSetupInstructions {row} />
				<AgentKeyRegenDialog {row} {rows} />
			{/if}
			<DropdownMenuSeparator />
			<TargetDeleteDialog {row} {rows} />
		</DropdownMenuGroup>
	{/snippet}
</ActionsBase>