<script lang="ts">
	import type { ModelChecker } from '$lib/api-client-axios';
	import { Checkbox } from '$lib/components/ui/checkbox';
	import { Label } from '$lib/components/ui/label';
	import type { Writable } from 'svelte/store';

	let { checker, checkers, initialState = false }: { checker: ModelChecker, checkers: Writable<number[]>, initialState?: boolean } = $props();

	const id = `checker-${checker.id}`
	let checked = $state(initialState);

	$effect(() => {
		if (checked) {
			checkers.update((arr) => [... arr, checker.id!]);
		} else {
			checkers.update((arr) => arr.filter((value) => value !== checker.id!));
		}
	})
</script>

<div class="flex flex-row gap-2">
	<Checkbox {id} bind:checked={checked}/>
	<Label for={id}>{`${checker.name}${checker.location ? ` - ${checker.location}` : ""}`}</Label>
</div>