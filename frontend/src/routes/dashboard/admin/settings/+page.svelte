<script lang="ts">
	import { additionalButtons, dashboardHeader } from '$lib/states';
	import { SvelteMap } from 'svelte/reactivity';
	import { settingsInfo } from '$lib/components/dashboard/admin/settings/settingsInfo';
	import SettingRow from '$lib/components/dashboard/admin/settings/SettingRow.svelte';

	let { data } = $props();

	dashboardHeader.set('Instance settings');
	additionalButtons.set(null);

	const kv = new SvelteMap<string, string | undefined>();

	data.settings.forEach((setting) => {
		kv.set(setting.key!, setting.value);
	});
</script>

<div class="flex flex-col gap-4">
	<p>Please note that some settings are read-only. They're used by checkmate internally to operate properly and aren't meant to be edited. </p>
	{#each settingsInfo.entries() as [key, info] (key)}
		<SettingRow {key} {info} initialValue={kv.get(key)}/>
	{/each}
</div>