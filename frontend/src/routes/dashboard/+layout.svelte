<script lang="ts">
	import { SidebarProvider, SidebarTrigger } from '$lib/components/ui/sidebar';
	import AppSidebar from '$lib/components/ui/sidebar/AppSidebar.svelte';
	import { dashboardHeader } from '$lib/states';

	let { data, children } = $props();
</script>

<SidebarProvider>
	<AppSidebar user={data.user} />
	<main class="p-4 w-full">
		<div class="flex items-center mb-4">
			<SidebarTrigger class="size-6" />
			<h1 class="inline text-2xl font-bold ml-8">{$dashboardHeader}</h1>
		</div>
		{@render children()}
	</main>
</SidebarProvider>
