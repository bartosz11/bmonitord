<script lang="ts">
	import type { SettingInfo } from '$lib/components/dashboard/admin/settings/settingsInfo';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { Button } from '$lib/components/ui/button';
	import { Save } from '@lucide/svelte';
	import { settingsApi } from '$lib/api';
	import { toast } from 'svelte-sonner';

	type SettingRowProps = {
		key: string,
		initialValue: string | undefined;
		info: SettingInfo,
	}
	let { key, initialValue, info }: SettingRowProps = $props();

	let value = info.valueConverter(initialValue);
	let checked = info.valueConverter(initialValue) === 'true';

	function onSave() {
		const val = info.inputType === 'checkbox' ? checked.toString() : value.toString();
		settingsApi.adminSettingsPost({
			key, value: val
		}).then((resp) => {
			if (resp.status === 200) {
				toast.success(`Successfully saved value of setting ${key}.`);
				// 	We don't need to update anything on the page
			}
		}).catch((err) => toast.error(`Failed to save value of setting ${key}: ` + (err.response?.data?.error ?? 'something went wrong')));
	}
</script>

<div class="grid grid-cols-6 items-center gap-4">
	<Label class="col-span-2">{info.displayKey}</Label>
	{#if info.inputType !== "checkbox"}
		<Input class="col-span-3" type={info.inputType} readonly={info.readonly} bind:value />
	{:else}
<!--		TODO: maybe find a solution later-->
<!--		For whatever shadcn's checkbox doesn't work when there's a binding to checked????-->
<!--		<Checkbox class="col-span-3" disabled={info.readonly} bind:checked={checked}></Checkbox>-->
		<input class="col-span-3 justify-self-start" type="checkbox" disabled={info.readonly} bind:checked={checked}>
	{/if}
	<Button class="w-fit col-span-1 justify-self-center" variant="outline" disabled={info.readonly} onclick={onSave}>
		<Save />
	</Button>
</div>