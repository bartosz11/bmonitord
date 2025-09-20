<script lang="ts">
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { Hint, required, useForm } from 'svelte-use-form';
	import { prefixes } from '$lib/validators';
	import { onMount } from 'svelte';
	import { subFormOutput, subFormState } from '$lib/components/dashboard/notifications/utils';

	let webhookURL = $state();

	const credentialsForm = useForm({}, "credentialsSubForm")

	onMount(() => {
		subFormState.set(credentialsForm);
		if ($subFormOutput) {
			webhookURL = $subFormOutput;
		} else {
			subFormOutput.set("");
		}
		return () => {
			subFormState.set(null);
			subFormOutput.set("");
		}
	})

	$effect(() => {
		subFormOutput.set(webhookURL);
	})
</script>

<form use:credentialsForm class="flex gap-4 flex-col mt-4">
	<Label>Webhook URL</Label>
	<Input type="text" name="webhookURL"
				 validators={[required, prefixes(["http://", "https://"])]} bind:value={webhookURL}></Input>
	<div>
		<Hint for="webhookURL" on="required" form="credentialsSubForm">Webhook URL is required.</Hint>
		<Hint for="webhookURL" on="prefixes" form="credentialsSubForm">Webhook URL must start with http:// or https://</Hint>
	</div>
</form>