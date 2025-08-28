<script lang="ts">
	import EllipsisIcon from '@lucide/svelte/icons/ellipsis';
	import { Button } from '$lib/components/ui/button/index.js';
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu/index.js';
	import type { ModelChecker } from '$lib/api-client-axios';
	import RegenerateKeyDialog from '$lib/components/dashboard/admin/checkers/RegenerateKeyDialog.svelte';
	import { toast } from 'svelte-sonner';
	import EditDialog from '$lib/components/dashboard/admin/checkers/EditDialog.svelte';
	import type { Writable } from 'svelte/store';
	import DeleteDialog from '$lib/components/dashboard/admin/checkers/DeleteDialog.svelte';

	let { row, rows }: { row: ModelChecker, rows: Writable<ModelChecker[]> } = $props();

	function onCopyKeyClick() {
		navigator.clipboard.writeText(row.key!)
		toast.success("Copied checker key to clipboard.");
	}
</script>

<DropdownMenu.Root>
	<DropdownMenu.Trigger>
		{#snippet child({ props })}
			<Button
				{...props}
				variant="ghost"
				size="icon"
				class="relative size-8 p-0"
			>
				<span class="sr-only">Open menu</span>
				<EllipsisIcon />
			</Button>
		{/snippet}
	</DropdownMenu.Trigger>
	<DropdownMenu.Content>
		<DropdownMenu.Group>
			<DropdownMenu.Label>Actions</DropdownMenu.Label>
			<DropdownMenu.Item onclick={onCopyKeyClick}>Copy key to clipboard</DropdownMenu.Item>
			<DropdownMenu.Separator />
			<EditDialog {row} {rows}/>
			<DropdownMenu.Separator />
			<RegenerateKeyDialog {row}/>
			<DeleteDialog {row} {rows}/>
		</DropdownMenu.Group>
	</DropdownMenu.Content>
</DropdownMenu.Root>