<script lang="ts">
	import * as Dialog from '$lib/components/ui/dialog/index.js';
	import { Button } from '$lib/components/ui/button';
	import { notNegative } from '$lib/validators';
	import { Hint, required, useForm } from 'svelte-use-form';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import type { ModelAlarm, ModelAlarmType, ModelNotification } from '$lib/api-client-axios';
	import { writable, type Writable } from 'svelte/store';
	import { toast } from 'svelte-sonner';
	import type { Snippet } from 'svelte';
	import {
		alarmSubFormOutput,
		alarmSubForms,
		alarmSubFormValidity,
		alarmTypeOptions
	} from '$lib/components/dashboard/alarms/utils';
	import AlarmNotificationToggle from '$lib/components/dashboard/alarms/AlarmNotificationToggle.svelte';
	import { alarmApi } from '$lib/api';

	let open = $state(false);

	//Yet another reusable form component
	let { alarms, alarm, notifications, targetId, trigger }: {
		alarms: Writable<ModelAlarm[]>,
		alarm?: ModelAlarm,
		notifications: ModelNotification[],
		targetId: number,
		trigger: Snippet
	} = $props();

	const form = useForm({
		name: {
			initial: alarm ? alarm.name! : undefined
		},
		maxRetries: {
			initial: alarm ? alarm.maxRetries!.toString() : "0"
		},
	}, 'alarmDataDialogForm');
	const notificationsSelectedBefore: Array<number | undefined> = alarm ? alarm.notifications!.map(n => n.id!) : [];

	// Use "threshold" type as default value, if user is trying to edit the default "unavailable" alarm, fine - nothing gets rendered, we can safely derive the SubForm from the existing alarm's type
	let type = $state(alarm ? alarm.type!.toString() : alarmTypeOptions[1].value);
	let SubForm = $derived(alarmSubForms[parseInt(type)]);
	let selectedNotifications = writable<number[]>([]);

	if (alarm) {
		alarmSubFormOutput.set(alarm);
		//data already present in the db pretty much has to be valid
		alarmSubFormValidity.set(true);
	}

	function onSubmit() {
		const reqBody = {
			name: $form.name.value,
			maxRetries: Number($form.maxRetries.value),
			notificationIDs: $selectedNotifications,
			type:  parseInt(type) as ModelAlarmType,
			...$alarmSubFormOutput
		};
		if (type === "0") {
			// @ts-expect-error it's not optional according to TS because it's defined above but in API specs it's optional in update, which is the only case it can even be 0 in this component
			delete reqBody.type;
		}
		const req = alarm ? alarmApi.targetTargetIDAlarmAlarmIDPatch(targetId, alarm.id!, reqBody) : alarmApi.targetTargetIDAlarmPost(targetId, reqBody);
		req.then((resp) => {
			if (resp.status === 201) {
				toast.success('Successfully created a new alarm.');
				alarms.update((alarmsArr) => [...alarmsArr, resp.data.data!]);
			}

			if (resp.status === 200) {
				toast.success('Successfully edited alarm.');
				alarms.update((arr) =>
					arr.map(a =>
						a.id! === alarm!.id! ? { ...a, ...resp.data.data! } : a
					));
			}
			open = false;
		}).catch((err) => toast.error(`Failed to ${alarm ? 'edit' : 'create'} alarm: ` + (err.response?.data?.error ?? 'something went wrong')));
	}
</script>

<Dialog.Root bind:open={open}>
	<Dialog.Trigger class={alarm ? "w-full" : ""}>
		{@render trigger()}
	</Dialog.Trigger>
	<Dialog.Content>
		<Dialog.Header>
			<Dialog.Title>{alarm ? `Edit ${alarm.name}` : "Create a new alarm"}</Dialog.Title>
		</Dialog.Header>
		<div>
			<form use:form class="flex gap-4 flex-col mt-2">
				<Label>Name</Label>
				<Input type="text" name="name" validators={[required]}></Input>
				<Hint for="name" on="required" form="alarmDataDialogForm">Name is required.</Hint>
				<!--	TODO: add a tooltip / some kind of helper here to explain this  -->
				<Label>Max retries</Label>
				<Input name="maxRetries" type="number" validators={[required, notNegative]}></Input>
				<div>
					<Hint for="maxRetries" form="alarmDataDialogForm" on="required">Max retries is required.</Hint>
					<Hint for="maxRetries" form="alarmDataDialogForm" on="notNegative">Max retries must be a non-negative number.</Hint>
				</div>
			</form>
			<SubForm></SubForm>
			<Label class="mt-4">Notifications</Label>
			<div class="mt-4 flex flex-row flex-wrap gap-4">
				{#each notifications as notification (notification.id)}
					<AlarmNotificationToggle {notification} notifications={selectedNotifications}
																	 initialState={alarm ? notificationsSelectedBefore.includes(notification.id) : false} />
				{/each}
			</div>
		</div>

		<Dialog.Footer>
			<Button type="button" variant="outline" class="mt-4" onclick={() => open = false}>Close</Button>
			<Button type="submit" class="mt-4"
							disabled={!($form.valid && $alarmSubFormValidity)}
							onclick={onSubmit}>{alarm ? "Edit" : "Create"}</Button>
		</Dialog.Footer>

	</Dialog.Content>
</Dialog.Root>