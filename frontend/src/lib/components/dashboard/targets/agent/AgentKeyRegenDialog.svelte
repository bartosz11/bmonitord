<script lang="ts">
	import AlertDialog from '$lib/components/ui/alert-dialog/AlertDialog.svelte';
	import type {  ModelTarget } from '$lib/api-client-axios';
	import { DropdownMenuItem } from '$lib/components/ui/dropdown-menu';
	import { targetApi } from '$lib/api';
	import { toast } from 'svelte-sonner';
	import type { Writable } from 'svelte/store';

	let { row, rows }: { row: ModelTarget, rows: Writable<ModelTarget[]> } = $props();

	let open = $state(false);

	function regenerate() {
		targetApi.targetTargetIDAgentKeyPatch(row.id!).then((resp) => {
			if (resp.status === 200) {
				toast.success('Successfully regenerated key.');
				rows.update((arr) =>
					arr.map(n =>
						n.id! === row!.id! ? { ...n, ...resp.data.data! } : n
					));
			}
		}).catch((err) => toast.error('Failed to regenerate key: ' + (err.response?.data?.error ?? 'something went wrong')))
			.finally(() => open = false);
	}
</script>

<AlertDialog title={`Are you sure you want to regenerate the key of ${row.name}?`}
						 description="This agent will not be able to report data again until you change the URL in its configuration! Do this only if the key may have gotten exposed to the public and data integrity is at risk."
						 continueOnClick={regenerate} bind:open triggerContainerClass="w-full">
	{#snippet trigger()}
		<DropdownMenuItem class="text-destructive" onSelect={(e) => e.preventDefault()}>Regenerate agent's key</DropdownMenuItem>
	{/snippet}
</AlertDialog>
