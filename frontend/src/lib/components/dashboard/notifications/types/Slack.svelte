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
		// clean up the states on destroy
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
	<Label>Slack webhook URL</Label>
	<Input type="text" name="slackWebhookURL"
				 validators={[required, prefixes(["https://hooks.slack.com/"])]}  bind:value={webhookURL}></Input>
	<div>
		<Hint for="slackWebhookURL" on="required" form="credentialsSubForm">Slack webhook URL is required.</Hint>
		<Hint for="slackWebhookURL" on="prefixes" form="credentialsSubForm">Slack webhook URL must start with https://hooks.slack.com/</Hint>
	</div>
</form>
