<script lang="ts">
	import { additionalButtons, dashboardHeader } from '$lib/states';
	import { Hint, required, useForm } from 'svelte-use-form';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { notNegative } from '$lib/validators';
	import { Select, SelectContent, SelectItem, SelectTrigger } from '$lib/components/ui/select';
	import {
		targetCreateSubFormOutput, targetCreateSubForms,
		targetCreateSubFormValidity, targetTypeOptions
	} from '$lib/components/dashboard/targets/utils';
	import TargetCheckerToggle from '$lib/components/dashboard/targets/TargetCheckerToggle.svelte';
	import { writable } from 'svelte/store';
	import { Button } from '$lib/components/ui/button';
	import { targetApi } from '$lib/api';
	import type { ModelTargetType } from '$lib/api-client-axios';
	import { toast } from 'svelte-sonner';
	import { goto } from '$app/navigation';

	let { data } = $props();

	dashboardHeader.set('Create a new target');
	additionalButtons.set(null);

	const form = useForm({
		name: {},
		maxRetries: {},
		timeout: {}
	}, 'createTargetForm');
	let type = $state(targetTypeOptions[0].value);

	let SubForm = $derived(targetCreateSubForms[parseInt(type)]);
	// update the select's thing
	const triggerContent = $derived(
		targetTypeOptions.find((opt) => opt.value === type)?.label
	);
	let checkers = writable<number[]>([]);

	function onSubmit() {
		targetApi.targetPost({
			name: $form.name.value,
			type: parseInt(type) as ModelTargetType,
			timeout: parseInt($form.timeout.value),
			maxRetries: parseInt($form.maxRetries.value),
			checkerIDs: $checkers,
			...		$targetCreateSubFormOutput
		}).then((resp) => {
			if (resp.status === 201) {
				toast.success("Successfully create a new target.");
				goto('/dashboard/targets');
			}
		}).catch((err) => toast.error('Failed to create target: ' + (err.response?.data?.error ?? 'something went wrong')))
	}
</script>

<div class="flex flex-col gap-4">
	<form use:form class="flex flex-col gap-4">
		<h2 class="text-xl font-semibold">General information</h2>
		<Label>Name</Label>
		<Input name="name" type="text" placeholder="My website" validators={[required]}></Input>
		<Hint for="name" on="required" form="createTargetForm">Name is required.</Hint>
		<Label>Type</Label>
		<Select name="type" bind:value={type} type="single">
			<SelectTrigger class="w-full">
				{triggerContent}
			</SelectTrigger>
			<SelectContent>
				{#each targetTypeOptions as option (option.value)}
					<SelectItem label={option.label} value={option.value}>{option.label}</SelectItem>
				{/each}
			</SelectContent>
		</Select>
		<!--	TODO: add a tooltip / some kind of helper here to explain these two -->
		<Label>Max retries</Label>
		<Input name="maxRetries" type="number" validators={[required, notNegative]}></Input>
		<div>
			<Hint for="maxRetries" form="createTargetForm" on="required">Max retries is required.</Hint>
			<Hint for="maxRetries" form="createTargetForm" on="notNegative">Max retries must be a non-negative number.</Hint>
		</div>
		<Label>Timeout (seconds)</Label>
		<Input name="timeout" type="number" validators={[required, notNegative]}></Input>
		<div>
			<Hint for="timeout" form="createTargetForm" on="required">Timeout is required.</Hint>
			<Hint for="timeout" form="createTargetForm" on="notNegative">Timeout must be a non-negative number.</Hint>
		</div>
	</form>

	<SubForm />

	<div class="flex flex-col gap-4">
		<div>
			<h2 class="text-xl font-semibold">Locations</h2>
			<p class="mt-2 text-sm">You must select at least one location.</p>
		</div>

		<div class="flex flex-row flex-wrap gap-4">
			{#each data.checkers as checker (checker.id)}
				<TargetCheckerToggle {checker} {checkers} />
			{/each}
		</div>
	</div>

	<Button disabled={!($targetCreateSubFormValidity && $checkers.length !== 0)} onclick={onSubmit}>Create</Button>
</div>