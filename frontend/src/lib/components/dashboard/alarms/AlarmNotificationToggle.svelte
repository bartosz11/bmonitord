<script lang="ts">
	import type { ModelNotification } from '$lib/api-client-axios';
	import { Checkbox } from '$lib/components/ui/checkbox';
	import { Label } from '$lib/components/ui/label';
	import type { Writable } from 'svelte/store';

	let { notification, notifications, initialState = false }: { notification: ModelNotification, notifications: Writable<number[]>, initialState?: boolean } = $props();

	const id = `notification-${notification.id}`
	let checked = $state(initialState);

	$effect(() => {
		if (checked) {
			notifications.update((arr) => [... arr, notification.id!]);
		} else {
			notifications.update((arr) => arr.filter((value) => value !== notification.id!));
		}
	})
</script>

<div class="flex flex-row gap-2">
	<Checkbox {id} bind:checked={checked}/>
	<Label for={id}>{notification.name}</Label>
</div>