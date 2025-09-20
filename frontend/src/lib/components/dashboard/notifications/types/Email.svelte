<script lang="ts">
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { email, Hint, required, useForm } from 'svelte-use-form';
	import { onMount } from 'svelte';
	import { subFormOutput, subFormState } from '$lib/components/dashboard/notifications/utils';

	let emailAddr = $state();

	const credentialsForm = useForm({}, 'credentialsSubForm');

	onMount(() => {
		subFormState.set(credentialsForm);
		if ($subFormOutput) {
			emailAddr = $subFormOutput;
		} else {
			subFormOutput.set("");
		}
		return () => {
			subFormState.set(null);
			subFormOutput.set('');
		};
	});

	$effect(() => {
		subFormOutput.set(emailAddr);
	});
</script>

<form use:credentialsForm class="flex gap-4 flex-col mt-4">
	<Label>Email address</Label>
	<Input type="text" name="emailAddress" validators={[required, email]} bind:value={emailAddr}></Input>
	<div>
		<Hint for="emailAddress" on="required" form="credentialsSubForm">Email address is required.</Hint>
		<Hint for="emailAddress" on="email" form="credentialsSubForm">Email address must be valid.</Hint>
	</div>
</form>