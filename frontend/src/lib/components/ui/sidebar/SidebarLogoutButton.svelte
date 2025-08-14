<script lang="ts">
	import { LogOutIcon } from '@lucide/svelte';
	import { Button } from '../button';
	import { sessionApi } from '$lib/api';
	import { toast } from 'svelte-sonner';
	import Cookies from 'js-cookie';
	import { goto } from '$app/navigation';

	async function onClick() {
		sessionApi
			.sessionLogoutPost()
			.then((resp) => {
				if (resp.status === 204) {
					toast.success('Successfully logged you out.');
					Cookies.remove('auth-token');
					goto('/auth/login');
				}
			})
			.catch((err) => toast.error('Failed to log you out: ' + (err.response?.data?.error ?? 'something went wrong')));
	}
</script>

<Button variant="outline" onclick={onClick} size="sm">
	<LogOutIcon />
</Button>
