<script lang="ts">
	import * as AlertDialog from "$lib/components/ui/alert-dialog/index.js";
	import type { Snippet } from 'svelte';

	let { trigger, title, description, cancelText, continueText, continueOnClick }: AlertDialogProps = $props();
	type AlertDialogProps = {
		trigger: Snippet,
		title: string,
		description?: string,
		cancelText?: string,
		continueText?: string,
		continueOnClick:() => void
	}
</script>

<AlertDialog.Root>
	<AlertDialog.Trigger>{@render trigger()}</AlertDialog.Trigger>
	<AlertDialog.Content>
		<AlertDialog.Header>
			<AlertDialog.Title>{title}</AlertDialog.Title>
			{#if description}
				<AlertDialog.Description>{description}</AlertDialog.Description>
			{/if}
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel>{cancelText ? cancelText : "Cancel"}</AlertDialog.Cancel>
			<AlertDialog.Action onclick={continueOnClick}>{continueText ? continueText : "Continue"}</AlertDialog.Action>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>