<script lang="ts">
	import { Hint, required, useForm } from 'svelte-use-form';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { notNegative } from '$lib/validators';
	import { Select, SelectContent, SelectItem, SelectTrigger } from '$lib/components/ui/select';
	import {
		getTypeSpecificInfo,
		targetSubFormOutput, targetSubForms,
		targetSubFormValidity, targetTypeOptions
	} from '$lib/components/dashboard/targets/utils';
	import TargetCheckerToggle from '$lib/components/dashboard/targets/TargetCheckerToggle.svelte';
	import { writable } from 'svelte/store';
	import { Button } from '$lib/components/ui/button';
	import { targetApi } from '$lib/api';
	import type { ModelChecker, ModelTarget, ModelTargetType } from '$lib/api-client-axios';
	import { toast } from 'svelte-sonner';
	import { goto } from '$app/navigation';

	let { target, locations }: { target?: ModelTarget, locations: ModelChecker[] } = $props();

	// Similar to NotificationDataDialog, this is reused between create and edit actions for target
	// If target is null-ish it's in create mode, otherwise in edit mode

	// Just the "|undefined" is just for TS to stop screaming at me in TargetCheckerToggle line
	const targetCheckerIDs: Array<number | undefined> = target ? target.checkers!.map((checker) => checker.id!) : [];
	const form = useForm({
		name: {
			initial: target ? target.name!.toString() : undefined
		},
		maxRetries: {
			initial: target ? target.maxRetries!.toString() : undefined
		},
		timeout: {
			initial: target ? target.timeout!.toString() : undefined
		}
	}, 'targetDataForm');
	let type = $state(target ? target.type!.toString() : targetTypeOptions[0].value);
	if (target) {
		targetSubFormOutput.set(getTypeSpecificInfo(target));
		// There's simply no way we let invalid data into the DB and back to the user
		targetSubFormValidity.set(true);
	}

	let SubForm = $derived(targetSubForms[parseInt(type)]);
	// update the select's thing
	const triggerContent = $derived(
		targetTypeOptions.find((opt) => opt.value === type)?.label
	);
	// We don't handle initialization in edit mode here, check initialState attribute of TargetCheckerToggle
	// if it's enabled, it'll add itself to this store automatically - this way we don't have to deal with stuff like duplicate elements
	let checkers = writable<number[]>([]);

	function onSubmit() {
		const output = {
			name: $form.name.value,
			timeout: parseInt($form.timeout.value),
			maxRetries: parseInt($form.maxRetries.value),
			checkerIDs: $checkers,
			...$targetSubFormOutput
		};
		console.log(output);
		const req = target ? targetApi.targetTargetIDPatch(target.id!, output) : targetApi.targetPost({ type: parseInt(type) as ModelTargetType, ...output });
		req.then((resp) => {
			//Update doesn't use 201 and create doesn't use 200, so we can do sth like this instead of some pointless if (target) ... else ...
			if (resp.status === 200) {
				toast.success('Successfully updated target.');
				goto('/dashboard/targets');
			}
			if (resp.status === 201) {
				toast.success('Successfully created a new target.');
				goto('/dashboard/targets');
			}
		}).catch((err) => toast.error(`Failed to ${target ? "update" : "create"} target: ` + (err.response?.data?.error ?? 'something went wrong')));
	}
</script>

<div class="flex flex-col gap-4">
	<form use:form class="flex flex-col gap-4">
		<h2 class="text-xl font-semibold">General information</h2>
		<Label>Name</Label>
		<Input name="name" type="text" placeholder="My website" validators={[required]}></Input>
		<Hint for="name" on="required" form="targetDataForm">Name is required.</Hint>
		<!-- Type cannot be modified hence the check -->
		{#if !target}
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
		{/if}
		<!--	TODO: add a tooltip / some kind of helper here to explain these two -->
		<Label>Max retries</Label>
		<Input name="maxRetries" type="number" validators={[required, notNegative]}></Input>
		<div>
			<Hint for="maxRetries" form="targetDataForm" on="required">Max retries is required.</Hint>
			<Hint for="maxRetries" form="targetDataForm" on="notNegative">Max retries must be a non-negative number.</Hint>
		</div>
		<Label>Timeout (seconds)</Label>
		<Input name="timeout" type="number" validators={[required, notNegative]}></Input>
		<div>
			<Hint for="timeout" form="targetDataForm" on="required">Timeout is required.</Hint>
			<Hint for="timeout" form="targetDataForm" on="notNegative">Timeout must be a non-negative number.</Hint>
		</div>
	</form>

	<SubForm />

	<div class="flex flex-col gap-4">
		<div>
			<h2 class="text-xl font-semibold">Locations</h2>
			<p class="mt-2 text-sm">You must select at least one location.</p>
		</div>
		<div class="flex flex-row flex-wrap gap-4">
			{#each locations as checker (checker.id)}
				<TargetCheckerToggle {checker} {checkers}
														 initialState={target ? targetCheckerIDs.includes(checker.id) : false} />
			{/each}
		</div>
	</div>

	<Button disabled={!($form.valid && $targetSubFormValidity && $checkers.length !== 0)}
					onclick={onSubmit}>{target ? "Edit" : "Create"}</Button>
</div>