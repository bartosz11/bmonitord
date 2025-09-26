<script lang="ts">
	import AlertDialog from '$lib/components/ui/alert-dialog/AlertDialog.svelte';
	import type { ModelTarget } from '$lib/api-client-axios';
	import { DropdownMenuItem } from '$lib/components/ui/dropdown-menu';
	import { targetApi } from '$lib/api';
	import { toast } from 'svelte-sonner';
	import type { Writable } from 'svelte/store';

	let { row, rows }: { row: ModelTarget, rows: Writable<ModelTarget[]> } = $props();

	let open = $state(false);

	function onContinueClick() {
		targetApi.targetTargetIDDelete(row.id!).then((resp) => {
			if (resp.status === 204) {
				toast.success('Successfully deleted target.');
				rows.update((arr) => arr.filter((t) => t.id !== row.id!));
			}
		}).catch((err) => toast.error('Failed to delete target: ' + (err.response?.data?.error ?? 'something went wrong')))
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
