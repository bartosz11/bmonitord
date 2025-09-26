<script lang="ts">
	import { Hint, required, useForm } from 'svelte-use-form';
	import {
		httpCodes,
		targetCreateSubFormOutput,
		targetCreateSubFormValidity
	} from '$lib/components/dashboard/targets/utils';
	import * as Select from "$lib/components/ui/select/index.js";
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { Checkbox } from '$lib/components/ui/checkbox';
	import { prefixes } from '$lib/validators';

	const form = useForm({}, "httpSubForm");

	let selectedHTTPCodes = $state(["200"]);
	let selectTouched = $state(false);
	const triggerContent = $derived.by(() => {
		if (selectedHTTPCodes.length === 0) return "Select HTTP codes"
		else if (selectedHTTPCodes.length === 1) return selectedHTTPCodes[0];
		else return selectedHTTPCodes.join(", ");
	});
	let selectedCodesAmt = $derived(selectedHTTPCodes.length);

	let followRedirects = $state(true);
	let verifySSLCert = $state(false);
	let host = $state();

	$effect(() => {
		targetCreateSubFormOutput.set({httpInfo: {followRedirects, verifySSLCert, host, allowedCodes: selectedHTTPCodes.join(" ")}})
	})

	$effect(() => {
		targetCreateSubFormValidity.set($form.valid && selectedCodesAmt >= 1);
	})
</script>

<form use:form class="flex flex-col gap-4">
	<h2 class="text-lg font-semibold">HTTP options</h2>
	<Label>Host</Label>
	<Input name="host" placeholder="http://example.com" type="text" bind:value={host} validators={[required, prefixes(["http://", "https://"])]}></Input>
	<div>
		<Hint for="host" form="httpSubForm" on="required">Host is required.</Hint>
		<Hint for="host" form="httpSubForm" on="prefixes">Host must start with http:// or https://</Hint>
	</div>
	<Label>Allowed HTTP response codes</Label>
	<Select.Root type="multiple" bind:value={selectedHTTPCodes} onOpenChange={() => selectTouched = true}>
		<Select.Trigger class="w-full">
			{triggerContent}
		</Select.Trigger>
		<Select.Content>
			<Select.Group>
				<Select.Label>HTTP codes</Select.Label>
				{#each httpCodes as code (code)}
					<Select.Item value={code} label={code}>
						{code}
					</Select.Item>
				{/each}
			</Select.Group>
		</Select.Content>
	</Select.Root>
	{#if selectTouched && selectedCodesAmt === 0}
		<!--Basically a SUF hint without the SUF part-->
		<div class="svelte-use-form-hint">At least one HTTP response code must be selected.</div>
	{/if}
	<div class="flex flex-row gap-2">
		<Checkbox id="followRedirects" bind:checked={followRedirects} />
		<Label for="followRedirects">Follow redirects</Label>
	</div>
	<div class="flex flex-row gap-2">
		<Checkbox id="verifySSLCert" bind:checked={verifySSLCert} />
		<Label for="verifySSLCert">Check SSL certificate validity</Label>
	</div>
</form>