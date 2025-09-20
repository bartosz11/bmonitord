<script lang="ts">
	import * as Dialog from '$lib/components/ui/dialog/index.js';
	import { Button } from '$lib/components/ui/button';
	import { Hint, required, useForm } from 'svelte-use-form';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import type { ModelNotification, ModelNotificationType } from '$lib/api-client-axios';
	import type { Writable } from 'svelte/store';
	import { Select, SelectContent, SelectItem, SelectTrigger } from '$lib/components/ui/select/index';
	import {
		notificationTypeFormParts,
		notificationTypeOptions,
		subFormState, subFormOutput
	} from '$lib/components/dashboard/notifications/utils';
	import { notificationApi } from '$lib/api';
	import { toast } from 'svelte-sonner';
	import type { Snippet } from 'svelte';

	let open = $state(false);

	//This whole dialog is built to be re-usable between the create and edit actions, hence the props and the name
	//Basically if notification is not nullish it's in edit mode, otherwise in create mode
	let { notifications, notification, trigger }: { notifications: Writable<ModelNotification[]>, notification?: ModelNotification, trigger: Snippet } = $props();

	const form = useForm({
		name: {}
	}, 'createNotificationForm');

	let name = $state();
	// Setting the default value so we don't have to try to apply [required] validator
	let type = $state(notification ? notification.type!.toString() : notificationTypeOptions[0].value);
	let SubForm = $derived(notificationTypeFormParts[parseInt(type)]);
	// update the select's thing
	const triggerContent = $derived(
		notificationTypeOptions.find((opt) => opt.value === type)?.label
	);
	let subFormValid = $state(false);
	subFormState.subscribe((v) => {
		// v represents the "readable" returned by SUF's useForm action
		// This is fine and prevents a crash - if there has been no form yet, trigger btn wouldn't render without this condition
		// eslint-disable-next-line
		if (!v) return;
		v.subscribe((form) => {
			//Form is the actual SUF Form object - we have to check if it hasn't changed to undefined and if it's valid
			subFormValid = form && form.valid;
		})
	})

	if (notification) {
		name = notification.name!;
		subFormOutput.set(notification.credentials!);
	}

	function onSubmit() {
		const reqBody = {
			name: $form.name.value,
			type: parseInt(type) as ModelNotificationType,
			credentials: $subFormOutput
		};
		const req = notification ? notificationApi.notificationIdPatch(notification.id!, reqBody) : notificationApi.notificationPost(reqBody)
		req.then((resp) => {
			if (resp.status === 201) {
				toast.success('Successfully created a new notification.');
				notifications.update((n) => [...n, resp.data.data!]);
			}
			//The edit success case, they can pretty much live alongside each other
			if (resp.status === 200) {
				toast.success('Successfully edited notification.');
				notifications.update((arr) =>
					arr.map(n =>
						n.id! === notification!.id! ? { ...n, ...resp.data.data! } : n
					));
			}
			open = false;
		}).catch((err) => toast.error(`Failed to ${notification ? "edit" : "create"} notification: ` + (err.response?.data?.error ?? 'something went wrong')));
	}
</script>

<Dialog.Root bind:open={open}>
	<Dialog.Trigger class={notification ? "w-full" : ""}>
		{@render trigger()}
	</Dialog.Trigger>
	<Dialog.Content>
		<Dialog.Header>
			<Dialog.Title>{notification ? `Edit ${notification.name}` : "Create a new notification"}</Dialog.Title>
		</Dialog.Header>
		<div>
			<form use:form class="flex gap-4 flex-col mt-2">
				<Label>Name</Label>
				<Input type="text" placeholder="My Discord webhook" name="name" validators={[required]} bind:value={name}></Input>
				<Hint for="name" on="required" form="createNotificationForm">Name is required.</Hint>
				<Label>Type</Label>
				<Select name="type" bind:value={type} type="single">
					<SelectTrigger class="w-full">
						{triggerContent}
					</SelectTrigger>
					<SelectContent>
						{#each notificationTypeOptions as option(option.value)}
							<SelectItem label={option.label} value={option.value}>{option.label}</SelectItem>
						{/each}
					</SelectContent>
				</Select>
			</form>
			<SubForm></SubForm>
		</div>

		<Dialog.Footer>
			<Button type="button" variant="outline" class="mt-4" onclick={() => open = false}>Close</Button>
			<Button type="submit" class="mt-4" disabled={!$form.valid || !subFormValid} onclick={onSubmit}>{notification ? "Edit" : "Create"}</Button>
		</Dialog.Footer>

	</Dialog.Content>
</Dialog.Root>