<script lang="ts">
	import type { ModelOrchestrator } from '$lib/api-client-axios';
	import { toast } from 'svelte-sonner';
	import type { Writable } from 'svelte/store';
	import ActionsBase from '$lib/components/dashboard/ActionsBase.svelte';
	import OrchestratorDeleteDialog from '$lib/components/dashboard/admin/orchestrators/OrchestratorDeleteDialog.svelte';
	import {
		DropdownMenuGroup,
		DropdownMenuItem,
		DropdownMenuLabel,
		DropdownMenuSeparator
	} from '$lib/components/ui/dropdown-menu';

	let { row, rows }: { row: ModelOrchestrator, rows: Writable<ModelOrchestrator[]> } = $props();

	function onCopyHostClick() {
		navigator.clipboard.writeText(row.host!);
		toast.success('Copied orchestrator host to clipboard.');
	}
</script>

<ActionsBase>
	{#snippet content()}
		<DropdownMenuGroup>
			<DropdownMenuLabel>Actions</DropdownMenuLabel>
			<DropdownMenuItem onclick={onCopyHostClick}>Copy host to clipboard</DropdownMenuItem>
			<DropdownMenuSeparator />
			<!-- This is a nasty workaround to visually "gray-out" the option when it can't even be used, if I just rendered OrchestratorDeleteDialog the onClick even would've worked and opened the dialog anyway, which is unwanted	-->
			{#if row.system}
				<DropdownMenuItem class="text-destructive" disabled>Delete</DropdownMenuItem>
			{:else}
				<OrchestratorDeleteDialog {row} {rows}/>
			{/if}
		</DropdownMenuGroup>
	{/snippet}
</ActionsBase>