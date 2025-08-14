<script lang="ts">
	import { Hint, HintGroup, pattern, required, useForm } from 'svelte-use-form';
	import Card from '$lib/components/ui/card/Card.svelte';
	import { Button } from '$lib/components/ui/button/index.js';
	import { Label } from '$lib/components/ui/label/index.js';
	import { Input } from '$lib/components/ui/input/index.js';
	import { userApi } from '$lib/api.js';
	import { toast } from 'svelte-sonner';
	import Cookies from 'js-cookie';
	import { goto } from '$app/navigation';
	import { cn } from '$lib/utils';

	let { class: className }: { class?: string } = $props();

	const changePasswordForm = useForm({}, 'changePassword');

	async function onChangePasswordSubmit(e: SubmitEvent) {
		e.preventDefault();
		const oldPassword = $changePasswordForm.currentPassword!.value;
		const newPassword = $changePasswordForm.currentPassword!.value;
		userApi.userPasswordPatch({ oldPassword, newPassword }).then((resp) => {
			if (resp.status === 200) {
				toast.success('Your password has been successfully changed. Please sign in again using your new password.');
				Cookies.remove('auth-token');
				goto('/auth/login');
			}
		}).catch((err) => toast.error('Failed to change your password: ' + (err.response?.data?.error ?? 'something went wrong')));
	}
</script>

<Card title="Change your password"
			description="All login sessions previously started will become invalid after changing your password."
			class={cn("md:min-w-72", className)}>
	{#snippet content()}
		<form use:changePasswordForm onsubmit={onChangePasswordSubmit} class="grid gap-2">
			<Label for="currentPassword">Current password</Label>
			<Input type="password" name="currentPassword" validators={[required]}></Input>
			<Hint form="changePassword" for="currentPassword" on="required">Current password is required.</Hint>
			<Label for="newPassword">New password</Label>
			<Input type="password" name="newPassword"
						 validators={[required, pattern("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")]}></Input>
			<HintGroup form="changePassword" for="newPassword">
				<Hint form="changePassword" on="required">New password is required.</Hint>
				<Hint form="changePassword" on="pattern" class="lg:max-w-72">New password must be at least 8 characters long, contain a lowercase
					letter, an uppercase letter and a digit.
				</Hint>
			</HintGroup>
			<Button type="submit" disabled={!$changePasswordForm.valid} class="mt-2 w-full">Change</Button>
		</form>
	{/snippet}
</Card>