<script lang="ts">
	import type { ModelChecker, ModelTarget } from '$lib/api-client-axios';
	import { Checkbox } from '$lib/components/ui/checkbox';
	import { Label } from '$lib/components/ui/label';
	import type { Writable } from 'svelte/store';

	let { checker, target, checkers }: { checker: ModelChecker, target?: ModelTarget, checkers: Writable<number[]> } = $props();

	const id = `checker-${checker.id}-target-${target ? target.id! : "0"}`
	let checked = $state(false);

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