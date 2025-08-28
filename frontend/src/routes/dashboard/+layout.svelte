<script lang="ts">
	import { SidebarProvider, SidebarTrigger } from '$lib/components/ui/sidebar';
	import AppSidebar from '$lib/components/ui/sidebar/AppSidebar.svelte';
	import { additionalButtons, dashboardHeader } from '$lib/states';

	let { data, children } = $props();

	let user = data.user!;
</script>

<SidebarProvider>
	<AppSidebar {user} />
	<main class="p-4 w-full">
		<div class="flex items-center mb-4 md:gap-8 gap-4 flex-wrap">
			<SidebarTrigger class="size-6" />
			<h1 class="inline text-2xl font-bold">{$dashboardHeader}</h1>
			{@render $additionalButtons?.()}
		</div>
		{@render children()}
	</main>
</SidebarProvider>
