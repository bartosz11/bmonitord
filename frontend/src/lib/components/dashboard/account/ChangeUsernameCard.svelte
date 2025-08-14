<script lang="ts">
	import { Hint, required, useForm } from 'svelte-use-form';
	import Card from '$lib/components/ui/card/Card.svelte';
	import { Button } from '$lib/components/ui/button/index.js';
	import { Label } from '$lib/components/ui/label/index.js';
	import { Input } from '$lib/components/ui/input/index.js';
	import { userApi } from '$lib/api.js';
	import { toast } from 'svelte-sonner';
	import Cookies from 'js-cookie';
	import { goto } from '$app/navigation';
	import { cn } from '$lib/utils';

	const changeUsernameForm = useForm({}, 'changeUsername');

	let { class: className }: { class?: string } = $props();

	async function onChangeUsernameSubmit(e: SubmitEvent) {
		e.preventDefault();
		const newUsername = $changeUsernameForm.newUsername!.value;
		userApi.userUsernamePatch({ newUsername }).then((resp) => {
			if (resp.status === 200) {
				toast.success('Your username has been successfully changed to ' + newUsername + '. Please sign in again using your new username.');
				Cookies.remove('auth-token');
				goto('/auth/login');
			}
		}).catch((err) => toast.error('Failed to change your username: ' + (err.response?.data?.error ?? 'something went wrong')));
	}
</script>

<Card title="Change your username"
			description="All login sessions previously started will become invalid after changing your username."
			class={cn("md:min-w-72", className)}>
	{#snippet content()}
		<form name="changeUsername" use:changeUsernameForm onsubmit={onChangeUsernameSubmit}>
			<Label for="newUsername">New username</Label>
			<Input type="text" name="newUsername" class="mt-2" validators={[required]}></Input>
			<Hint form="changeUsername" for="newUsername" on="required" class="mt-2">New username cannot be blank.</Hint>
			<Button type="submit" disabled={!$changeUsernameForm.valid} class="mt-4 w-full">Change</Button>
		</form>
	{/snippet}
</Card>
