<script lang="ts">
	import type { ModelChecker } from '$lib/api-client-axios';
	import CheckerRegenerateKeyDialog from '$lib/components/dashboard/admin/checkers/CheckerRegenerateKeyDialog.svelte';
	import { toast } from 'svelte-sonner';
	import CheckerEditDialog from '$lib/components/dashboard/admin/checkers/CheckerEditDialog.svelte';
	import type { Writable } from 'svelte/store';
	import CheckerDeleteDialog from '$lib/components/dashboard/admin/checkers/CheckerDeleteDialog.svelte';
	import ActionsBase from '$lib/components/dashboard/ActionsBase.svelte';
	import {
		DropdownMenuGroup,
		DropdownMenuItem,
		DropdownMenuLabel,
		DropdownMenuSeparator
	} from '$lib/components/ui/dropdown-menu';

	let { row, rows }: { row: ModelChecker, rows: Writable<ModelChecker[]> } = $props();

	function onCopyKeyClick() {
		navigator.clipboard.writeText(row.key!);
		toast.success('Copied checker key to clipboard.');
	}
</script>

<ActionsBase>
	{#snippet content()}
		<DropdownMenuGroup>
			<DropdownMenuLabel>Actions</DropdownMenuLabel>
			<DropdownMenuItem onclick={onCopyKeyClick}>Copy key to clipboard</DropdownMenuItem>
			<DropdownMenuSeparator />
			<CheckerEditDialog {row} {rows} />
			<DropdownMenuSeparator />
			<CheckerRegenerateKeyDialog {row} />
			<CheckerDeleteDialog {row} {rows} />
		</DropdownMenuGroup>
	{/snippet}
</ActionsBase>