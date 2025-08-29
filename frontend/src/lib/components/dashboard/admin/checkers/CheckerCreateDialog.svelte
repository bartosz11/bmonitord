<script lang="ts">
	import * as Dialog from '$lib/components/ui/dialog/index.js';
	import { Button } from '$lib/components/ui/button';
	import { Hint, required, useForm } from 'svelte-use-form';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { checkerApi } from '$lib/api';
	import { toast } from 'svelte-sonner';
	import type { ModelChecker } from '$lib/api-client-axios';
	import type { Writable } from 'svelte/store';

	let open = $state(false);

	let { checkers }: { checkers: Writable<ModelChecker[]> } = $props();

	const form = useForm({
		name: {},
		location: {}
	});

	function onSubmit(e: SubmitEvent) {
		e.preventDefault();
		checkerApi.adminCheckerPost({
			name: $form.name.value,
			location: $form.location.value
		}).then((resp) => {
			if (resp.status === 201) {
				toast.success('Successfully created a new checker.');
				open = false;
				checkers.update((c) => [...c, resp.data.data!])
			}
		}).catch((err) => toast.error('Failed to create checker: ' + (err.response?.data?.error ?? 'something went wrong')));
	}
</script>

<Dialog.Root bind:open={open}>
	<Dialog.Trigger>
		<Button>
			Create checker
		</Button>
	</Dialog.Trigger>
	<Dialog.Content>
		<Dialog.Header>
			<Dialog.Title>Create a new checker</Dialog.Title>
		</Dialog.Header>

		<form use:form onsubmit={onSubmit}>
			<div class="flex gap-4 flex-col mt-2">
				<Label>Name</Label>
				<Input type="text" placeholder="PL-WAW-1" name="name" validators={[required]}></Input>
				<Hint for="name" on="required">Name is required.</Hint>
				<Label>Location</Label>
				<Input type="text" placeholder="Warsaw, Poland" name="location"></Input>
			</div>

			<Dialog.Footer>
				<Button type="button" variant="outline" class="mt-4" onclick={() => open = false}>Close</Button>
				<Button type="submit" class="mt-4" disabled={!$form.valid}>Create</Button>
			</Dialog.Footer>
		</form>
	</Dialog.Content>
</Dialog.Root>