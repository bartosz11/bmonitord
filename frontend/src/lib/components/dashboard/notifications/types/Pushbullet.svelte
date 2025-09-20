<script lang="ts">
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { Hint, required, useForm } from 'svelte-use-form';
	import { onMount } from 'svelte';
	import { subFormOutput, subFormState } from '$lib/components/dashboard/notifications/utils';

	let accessToken = $state();

	const credentialsForm = useForm({}, 'credentialsSubForm');

	onMount(() => {
		subFormState.set(credentialsForm);
		if ($subFormOutput) {
			accessToken = $subFormOutput;
		} else {
			subFormOutput.set("");
		}
		return () => {
			subFormState.set(null);
			subFormOutput.set('');
		};
	});

	$effect(() => {
		subFormOutput.set(accessToken);
	});
</script>

<form use:credentialsForm class="flex gap-4 flex-col mt-4">
	<Label>Pushbullet access token</Label>
	<Input type="text" name="pushbulletAccessToken" validators={[required]} bind:value={accessToken}></Input>
	<div>
		<Hint for="pushbulletAccessToken" on="required" form="credentialsSubForm">Pushbullet access token is required.</Hint>
	</div>
</form>