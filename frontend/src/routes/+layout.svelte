<script lang="ts">
	import { ModeWatcher } from 'mode-watcher';
	import '../app.css';
	import { Toaster } from '$lib/components/ui/sonner/index';
	import {TooltipProvider} from '$lib/components/ui/tooltip';

	let { children } = $props();
</script>

<ModeWatcher />
<Toaster richColors />
<TooltipProvider>
	{@render children()}
</TooltipProvider>

<svelte:head>
	<title>checkmate</title>
</svelte:head>
