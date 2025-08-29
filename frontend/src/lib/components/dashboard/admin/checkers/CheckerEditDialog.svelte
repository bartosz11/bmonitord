<script lang="ts">
	import * as Dialog from '$lib/components/ui/dialog/index.js';
	import { Button } from '$lib/components/ui/button';
	import { Hint, required, useForm } from 'svelte-use-form';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { checkerApi } from '$lib/api';
	import { toast } from 'svelte-sonner';
	import type { ModelChecker } from '$lib/api-client-axios';
	import { DropdownMenuItem } from '$lib/components/ui/dropdown-menu';
	import type { Writable } from 'svelte/store';

	let open = $state(false);

	let { row, rows }: { row: ModelChecker, rows: Writable<ModelChecker[]> } = $props();

	const form = useForm({
		name: {
			initial: row.name
		},
		location: {
			initial: row.location
		}
	});

	function onSubmit(e: SubmitEvent) {
		e.preventDefault();
		// I know I have partial edits in the API but I cba playing around with it, esp in this case where there are only 2 editable props
		checkerApi.adminCheckerIdPatch(row.id!, {
			name: $form.name.value,
			location: $form.location.value
		}).then((resp) => {
			if (resp.status === 200) {
				toast.success('Successfully updated checker.');
				// update the record in the list for reactivity reasons
				rows.update((arr) =>
					arr.map(chk =>
						chk.id! === row.id! ? { ...chk, ...resp.data.data! } : chk
					));
			}
		}).catch((err) => {
			toast.error('Failed to update checker: ' + (err.response?.data?.error ?? 'something went wrong'))
		});
	}
</script>

<Dialog.Root bind:open={open}>
	<Dialog.Trigger class="w-full">
		<DropdownMenuItem class="" onSelect={(e) => e.preventDefault()}>Edit</DropdownMenuItem>
	</Dialog.Trigger>
	<Dialog.Content>
		<Dialog.Header>
			<Dialog.Title>Edit {row.name}</Dialog.Title>
		</Dialog.Header>

		<form use:form onsubmit={onSubmit}>
			<div class="flex gap-4 flex-col mt-2">
				<Label>Name</Label>
				<Input type="text" name="name" validators={[required]}></Input>
				<Hint for="name" on="required">Name is required.</Hint>
				<Label>Location</Label>
				<Input type="text" name="location"></Input>
			</div>

			<Dialog.Footer>
				<Button type="button" variant="outline" class="mt-4" onclick={() => open = false}>Close</Button>
				<Button type="submit" class="mt-4" disabled={!$form.valid}>Edit</Button>
			</Dialog.Footer>
		</form>
	</Dialog.Content>
</Dialog.Root>