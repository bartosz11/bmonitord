<script lang="ts">
	import * as AlertDialog from "$lib/components/ui/alert-dialog/index.js";
	import type { Snippet } from 'svelte';

	let { trigger, title, description, cancelText, continueText, continueOnClick, open = $bindable(false), triggerContainerClass }: AlertDialogProps = $props();

	type AlertDialogProps = {
		trigger: Snippet,
		title: string,
		description?: string,
		cancelText?: string,
		continueText?: string,
		continueOnClick:() => void,
		open?: boolean
		triggerContainerClass?: string,
	}
</script>

<AlertDialog.Root bind:open>
	<AlertDialog.Trigger class={triggerContainerClass}>{@render trigger()}</AlertDialog.Trigger>
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