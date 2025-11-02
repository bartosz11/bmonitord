<script lang="ts">
	import * as Dialog from '$lib/components/ui/dialog/index.js';
	import { Button } from '$lib/components/ui/button';
	import type { ModelTarget } from '$lib/api-client-axios';
	import { DropdownMenuItem } from '$lib/components/ui/dropdown-menu';
	import { ButtonGroup } from '$lib/components/ui/button-group/index.js';
	import { Input } from '$lib/components/ui/input';
	import { CopyIcon } from '@lucide/svelte';
	import { toast } from 'svelte-sonner';

	let open = $state(false);

	let { row }: { row: ModelTarget } = $props();

	const scriptURL = 'https://raw.githubusercontent.com/bartosz11/checkmate/refs/heads/v3/agent/agent.py';
	let postURL = $derived(`${window.location.origin}/orchestrator/agent/${row.agent!.key}`);

	function onScriptURLCopyClick() {
		navigator.clipboard.writeText(scriptURL).then(() => {
			toast.success("Copied script URL to clipboard!");
		})
	}

	function onPostURLCopyClick() {
		navigator.clipboard.writeText(postURL).then(() => {
			toast.success("Copied URL to clipboard!");
		})
	}
</script>

<Dialog.Root bind:open={open}>
	<Dialog.Trigger class="w-full">
		<DropdownMenuItem onSelect={(e) => e.preventDefault()}>Agent setup instructions</DropdownMenuItem>
	</Dialog.Trigger>
	<Dialog.Content>
		<Dialog.Header>
			<Dialog.Title>Agent setup instructions for {row.name}</Dialog.Title>
		</Dialog.Header>
		<div class="space-y-4 mt-4">
			<p>1. Install Python 3 and following modules with pip: <code>py-cpuinfo requests psutil</code>. If you're running a Linux distribution you also need to install the <code>distro</code> module.</p>
			<div>
				<p>2. Download the agent script from GitHub using the link below.</p>
				<ButtonGroup class="w-full mt-2">
					<Input value={scriptURL} readonly></Input>
					<Button variant="outline" onclick={onScriptURLCopyClick}>
						<CopyIcon />
					</Button>
				</ButtonGroup>
			</div>
			<div>
				<p>3. Open the agent script file with a text editor and change the value of the URL variable to the URL below.</p>
				<ButtonGroup class="w-full mt-2">
					<Input value={postURL} readonly></Input>
					<Button variant="outline" onclick={onPostURLCopyClick}>
						<CopyIcon />
					</Button>
				</ButtonGroup>
			</div>
			<p>4. Set up the agent script to run every minute with your system's scheduler, for example cron.</p>
		</div>
		<Dialog.Footer>
			<Button type="button" class="mt-4" onclick={() => open = false}>Close</Button>
		</Dialog.Footer>
	</Dialog.Content>
</Dialog.Root>