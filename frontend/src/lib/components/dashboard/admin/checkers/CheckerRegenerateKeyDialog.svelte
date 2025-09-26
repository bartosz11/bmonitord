<script lang="ts">
	import AlertDialog from '$lib/components/ui/alert-dialog/AlertDialog.svelte';
	import type { ModelChecker } from '$lib/api-client-axios';
	import { DropdownMenuItem } from '$lib/components/ui/dropdown-menu';
	import { checkerApi } from '$lib/api';
	import { toast } from 'svelte-sonner';

	let { row }: { row: ModelChecker } = $props();

	let open = $state(false);

	function regenerate() {
		checkerApi.checkerIdKeyPatch(row.id!).then((resp) => {
			if (resp.status === 200) {
				row.key = resp.data.data!.key!;
				toast.success('Successfully regenerated key.');
			}
		}).catch((err) => toast.error('Failed to regenerate key: ' + (err.response?.data?.error ?? 'something went wrong')))
			.finally(() => open = false);
	}
</script>

<AlertDialog title={`Are you sure you want to regenerate the key of ${row.name}?`}
						 description="This checker will not be able to operate again until you change the key in its configuration! Do this only if the key may have gotten exposed to the public and data integrity is at risk."
						 continueOnClick={regenerate} bind:open triggerContainerClass="w-full">
	{#snippet trigger()}
		<DropdownMenuItem class="text-destructive" onSelect={(e) => e.preventDefault()}>Regenerate key</DropdownMenuItem>
	{/snippet}
</AlertDialog>
