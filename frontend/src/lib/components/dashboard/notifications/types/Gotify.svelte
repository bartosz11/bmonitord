<script lang="ts">
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { Hint, required, useForm } from 'svelte-use-form';
	import { prefixes } from '$lib/validators';
	import { onMount } from 'svelte';
	import { subFormOutput, subFormState } from '$lib/components/dashboard/notifications/utils';

	let url = $state();
	let token = $state();

	const credentialsForm = useForm({}, 'credentialsSubForm');

	onMount(() => {
		subFormState.set(credentialsForm);
		if ($subFormOutput) {
			const strings = $subFormOutput.split(";");
			url = strings[0];
			token = strings[1];
		} else {
			subFormOutput.set("");
		}
		return () => {
			subFormState.set(null);
			subFormOutput.set('');
		};
	});

	$effect(() => {
		subFormOutput.set(`${url};${token}`);
	});
</script>

<form use:credentialsForm class="flex flex-col gap-4 mt-4">
	<Label>Gotify URL</Label>
	<Input type="text" name="gotifyURL"
				 validators={[required, prefixes(["http://", "https://"])]} bind:value={url}></Input>
	<div>
		<Hint for="gotifyURL" on="required" form="credentialsSubForm">Gotify URL is required.</Hint>
		<Hint for="gotifyURL" on="prefixes" form="credentialsSubForm">Gotify URL must start with http:// or https://</Hint>
	</div>
	<Label>Gotify token</Label>
	<Input type="text" name="gotifyToken" validators={[required]} bind:value={token}></Input>
	<div>
		<Hint for="gotifyToken" on="required" form="credentialsSubForm">Gotify token is required.</Hint>
	</div>
</form>