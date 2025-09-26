<script lang="ts">
	import AlertDialog from '$lib/components/ui/alert-dialog/AlertDialog.svelte';
	import type { ModelChecker } from '$lib/api-client-axios';
	import { DropdownMenuItem } from '$lib/components/ui/dropdown-menu';
	import { checkerApi } from '$lib/api';
	import { toast } from 'svelte-sonner';
	import type { Writable } from 'svelte/store';

	let { row, rows }: { row: ModelChecker, rows: Writable<ModelChecker[]> } = $props();

	let open = $state(false);

	function onContinueClick() {
		checkerApi.checkerIdDelete(row.id!).then((resp) => {
			if (resp.status === 204) {
				toast.success('Successfully deleted checker.');
				rows.update((arr) => arr.filter((chk) => chk.id !== row.id!));
			}
		}).catch((err) => toast.error('Failed to delete checker: ' + (err.response?.data?.error ?? 'something went wrong')))
			.finally(() => open = false);
	}
</script>

<AlertDialog title={`Are you sure you want to delete ${row.name}?`}
						 description="This action is irreversible, although the data collected by this checker will remain in the database."
						 continueOnClick={onContinueClick} bind:open triggerContainerClass="w-full">
	{#snippet trigger()}
		<DropdownMenuItem class="text-destructive" onSelect={(e) => e.preventDefault()}>Delete</DropdownMenuItem>
	{/snippet}
</AlertDialog>
