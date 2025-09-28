<script lang="ts">
	import { Hint, required, useForm } from 'svelte-use-form';
	import {
		targetSubFormOutput,
		targetSubFormValidity
	} from '$lib/components/dashboard/targets/utils';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { onMount } from 'svelte';
	import type { ModelTargetPingInfo } from '$lib/api-client-axios';

	const form = useForm({}, 'pingSubForm');

	let host = $state();
	onMount(() => {
		let output = $targetSubFormOutput;
		if (typeof output === 'object' && "pingInfo" in output) {
			const pingInfo = output.pingInfo! as ModelTargetPingInfo;
			host = pingInfo.host!;
		}
	})

	$effect(() => {
		targetSubFormOutput.set({pingInfo: { host }});
	});

	$effect(() => {
		targetSubFormValidity.set($form.valid);
	});
</script>

<form use:form class="flex flex-col gap-4">
	<h2 class="text-lg font-semibold">Ping options</h2>
	<Label>Host</Label>
	<Input name="host" placeholder="1.1.1.1" type="text" bind:value={host} validators={[required]}></Input>
	<Hint for="host" form="pingSubForm" on="required">Host is required.</Hint>
</form>