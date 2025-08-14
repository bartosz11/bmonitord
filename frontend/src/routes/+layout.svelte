<script lang="ts">
	import { ModeWatcher } from 'mode-watcher';
	import '../app.css';
	import { Toaster } from '$lib/components/ui/sonner/index';

	let { children } = $props();
</script>

<ModeWatcher />
<Toaster richColors />
{@render children()}

<svelte:head>
	<title>checkmate</title>
</svelte:head>
