<script lang="ts">
	import * as Dialog from '$lib/components/ui/dialog/index.js';
	import { Button } from '$lib/components/ui/button';
	import { Hint, required, useForm } from 'svelte-use-form';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { orchestratorApi } from '$lib/api';
	import { toast } from 'svelte-sonner';
	import type { ModelOrchestrator } from '$lib/api-client-axios';
	import type { Writable } from 'svelte/store';
	import { prefixes } from '$lib/validators';

	let open = $state(false);

	let { orchestrators }: { orchestrators: Writable<ModelOrchestrator[]> } = $props();

	const form = useForm({
		name: {},
		host: {}
	});

	function onSubmit(e: SubmitEvent) {
		e.preventDefault();
		orchestratorApi.adminOrchestratorPost({
			name: $form.name.value,
			host: $form.host.value
		}).then((resp) => {
			if (resp.status === 201) {
				toast.success('Successfully created a new orchestrator.');
				open = false;
				orchestrators.update((o) => [...o, resp.data.data!]);
			}
		}).catch((err) => toast.error('Failed to create orchestrator: ' + (err.response?.data?.error ?? 'something went wrong')));
	}
</script>

<Dialog.Root bind:open={open}>
	<Dialog.Trigger>
		<Button>
			Create orchestrator
		</Button>
	</Dialog.Trigger>
	<Dialog.Content>
		<Dialog.Header>
			<Dialog.Title>Create a new orchestrator</Dialog.Title>
			<Dialog.Description>Both fields below must have unique values.</Dialog.Description>
		</Dialog.Header>

		<form use:form onsubmit={onSubmit}>
			<div class="flex gap-4 flex-col mt-2">
				<Label>Name</Label>
				<Input type="text" placeholder="PL-WAW-1" name="name" validators={[required]}></Input>
				<Hint for="name" on="required">Name is required.</Hint>
				<Label>Host</Label>
				<Input type="text" placeholder="wss://example.com" name="host"
							 validators={[required, prefixes(["ws://", "wss://"])]}></Input>
				<div>
					<Hint for="host" on="required">Host is required.</Hint>
					<Hint for="host" on="prefixes">Host must start with ws:// or wss://</Hint>
				</div>
			</div>

			<Dialog.Footer>
				<Button type="button" variant="outline" class="mt-4" onclick={() => open = false}>Close</Button>
				<Button type="submit" class="mt-4" disabled={!$form.valid}>Create</Button>
			</Dialog.Footer>
		</form>
	</Dialog.Content>
</Dialog.Root>