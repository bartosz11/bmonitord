<script lang="ts">
	import { additionalButtons, dashboardHeader } from '$lib/states';
	import SessionCard from '$lib/components/dashboard/sessions/SessionCard.svelte';
	import { Button } from '$lib/components/ui/button';
	import { LogOutIcon } from '@lucide/svelte';
	import { sessionApi } from '$lib/api';
	import Cookies from 'js-cookie';
	import { goto } from '$app/navigation';
	import { toast } from 'svelte-sonner';
	import type { ModelSession } from '$lib/api-client-axios/index.js';

	dashboardHeader.set('Your login sessions');
	additionalButtons.set(null);

	let { data } = $props();

	const sessions = data.sessions.sort(compareSessionsDesc);

	async function onLogoutAllClick() {
		sessionApi.sessionDelete().then((resp) => {
			if (resp.status === 204) {
				Cookies.remove('auth-token');
				goto('/auth/login');
				toast.success('Successfully logged out all login sessions.');
			}
		}).catch((err) => toast.error('Failed to log out session: ' + (err.response?.data?.error ?? 'something went wrong')));
	}

	function compareSessionsDesc(a: ModelSession, b: ModelSession) {
		return new Date(b.createdAt!).getTime() - new Date(a.createdAt!).getTime();
	}
</script>


<div class="flex-col flex gap-4">
	<div class="flex flex-col lg:flex-row gap-4 lg:items-center">
		<p>A session is considered invalid if it has expired or account credentials have changed since it was created.
			Invalid
			sessions cannot be used to access your account and they'll be removed from the database once there's such
			attempt.</p>
		<Button variant="secondary" onclick={onLogoutAllClick}>
			Log out all sessions
			<LogOutIcon />
		</Button>
	</div>
	{#each sessions as session (session)}
		<SessionCard session={session} />
	{/each}
</div>