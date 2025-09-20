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
	<Label>Discord webhook URL</Label>
	<Input type="text" name="discordWebhookURL"
				 validators={[required, prefixes(["https://discord.com/api/webhooks/"])]} bind:value={webhookURL}></Input>
	<div>
		<Hint for="discordWebhookURL" on="required" form="credentialsSubForm">Discord webhook URL is required.</Hint>
		<Hint for="discordWebhookURL" on="prefixes" form="credentialsSubForm">Discord webhook URL must start with https://discord.com/api/webhooks/</Hint>
	</div>
</form>