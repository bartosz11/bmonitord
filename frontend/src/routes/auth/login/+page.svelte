<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import Input from '$lib/components/ui/input/input.svelte';
	import Label from '$lib/components/ui/label/label.svelte';
	import { useForm, Hint, required } from 'svelte-use-form';
	import { LogIn } from '@lucide/svelte';
	import { authApi } from '$lib/api';
	import { toast } from 'svelte-sonner';
	import { goto } from '$app/navigation';

	const loginForm = useForm();

	async function onSubmit() {
		authApi.authLoginPost(
			{
				// These cannot be null since they are form elements
				username: $loginForm.username!.value,
				password: $loginForm.password!.value
			}
		).then(() => {
			toast.success('Logged in successfully.');
			goto("/dashboard/targets");
		}).catch(err => {
			toast.error("Login failed: " + (err.response?.data?.error ?? "something went wrong"));
		});
	}
</script>

<div class="container flex h-screen flex-col mx-auto items-center justify-center">
	<div>
		<h1 class="mb-12 text-center text-3xl font-bold">Sign in to checkmate</h1>
		<form use:loginForm on:submit|preventDefault={onSubmit} class="flex w-80 flex-col gap-2">
			<Label class="text-md" for="username">Username</Label>
			<Input type="text" name="username" validators={[required]} />
			<Hint for="username" on="required">Username is required.</Hint>
			<Label class="text-md" for="password">Password</Label>
			<Input type="password" name="password" validators={[required]} />
			<Hint for="password" on="required">Password is required.</Hint>
			<Button type="submit" disabled={!$loginForm.valid} class="mt-4">
				Log in
				<LogIn />
			</Button>
		</form>
		<Button class="mt-8 w-full text-center" variant="link" href="/auth/signup">Don't have an account yet?</Button>
	</div>
</div>
