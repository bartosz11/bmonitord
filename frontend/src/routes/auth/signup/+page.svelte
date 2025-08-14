<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import Input from '$lib/components/ui/input/input.svelte';
	import Label from '$lib/components/ui/label/label.svelte';
	import { useForm, Hint, required, HintGroup, pattern } from 'svelte-use-form';
	import { CircleArrowRight } from '@lucide/svelte';
	import { authApi } from '$lib/api';
	import { toast } from 'svelte-sonner';
	import { goto } from '$app/navigation';

	const signupForm = useForm();

	async function onSubmit() {
		authApi.authRegisterPost(
			{
				username: $signupForm.username!.value,
				password: $signupForm.password!.value
			}
		).then(() => {
			toast.success('Account created successfully.');
			goto('/auth/login');
		}).catch(err => {
			toast.error('Signup failed: ' + (err.response?.data?.error ?? 'something went wrong'));
		});
	}
</script>

<div class="container flex flex-col h-screen mx-auto items-center justify-center">
	<div>
		<h1 class="mb-12 text-center text-3xl font-bold">Sign up for checkmate</h1>
		<form use:signupForm on:submit|preventDefault={onSubmit} class="flex w-80 flex-col gap-2">
			<Label class="text-md" for="username">Username</Label>
			<Input type="text" name="username" validators={[required]} />
			<Hint for="username" on="required">Username must not be blank.</Hint>
			<Label class="text-md" for="password">Password</Label>
			<Input type="password" name="password"
						 validators={[required, pattern("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")]} />
			<div>
				<HintGroup for="password">
					<Hint on="required">Password is required.</Hint>
					<Hint on="pattern">Password must be at least 8 characters long, contain a lowercase letter, an uppercase
						letter and a digit.
					</Hint>
				</HintGroup>
			</div>
			<Button type="submit" disabled={!$signupForm.valid} class="mt-4">
				Sign up
				<CircleArrowRight />
			</Button>
		</form>
		<Button class="mt-8 w-full text-center" variant="link" href="/auth/login">Have an account already?</Button>
	</div>
</div>