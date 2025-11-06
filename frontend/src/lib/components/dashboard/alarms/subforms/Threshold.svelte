<script lang="ts">
	import { Hint, required, useForm } from 'svelte-use-form';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { onMount } from 'svelte';
	import { Select, SelectContent, SelectItem, SelectTrigger } from '$lib/components/ui/select';
	import { alarmThresholdFieldOptions } from '$lib/components/dashboard/alarms/thresholdFieldInfo';
	import type { ModelAlarm } from '$lib/api-client-axios';
	import { alarmSubFormOutput, alarmSubFormValidity } from '$lib/components/dashboard/alarms/utils';

	const form = useForm({}, 'thresholdSubForm');

	let threshold = $state();
	let thresholdField = $state(alarmThresholdFieldOptions[0].value);
	let thresholdFieldParams = $state("");
	const triggerContent = $derived(
		alarmThresholdFieldOptions.find((opt) => opt.value === thresholdField)?.label
	);
	onMount(() => {
		let output = $alarmSubFormOutput;
		if (typeof output === 'object' && "thresholdField" in output && "threshold" in output && "thresholdFieldParams" in output) {
			const alarm = output as ModelAlarm; // casting because TS tends to cry
			thresholdField = alarm.thresholdField!.toString();
			threshold = alarm.threshold!;
			thresholdFieldParams = alarm.thresholdFieldParams!;
		}
	})

	$effect(() => {
		alarmSubFormOutput.set({
			thresholdField: parseInt(thresholdField), threshold, thresholdFieldParams
		});
	});

	$effect(() => {
		alarmSubFormValidity.set($form.valid);
	});
</script>

<form use:form class="flex flex-col gap-4 mt-4">
	<Label>Threshold field</Label>
	<Select name="type" bind:value={thresholdField} type="single">
		<SelectTrigger class="w-full">
			{triggerContent}
		</SelectTrigger>
		<SelectContent>
			{#each alarmThresholdFieldOptions as option(option.value)}
				<SelectItem label={option.label} value={option.value}>{option.label}</SelectItem>
			{/each}
		</SelectContent>
	</Select>
	<Label>Threshold</Label>
	<Input name="threshold" placeholder="250" type="number" bind:value={threshold} validators={[required]}></Input>
	<Hint for="threshold" form="thresholdSubForm" on="required">Threshold is required.</Hint>
	<Label>Threshold field parameters</Label>
	<Input name="thresholdFieldParams" type="text" bind:value={thresholdFieldParams}></Input>
</form>