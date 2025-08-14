<script lang="ts">
	import Card from '$lib/components/ui/card/Card.svelte';
	import { Button } from '$lib/components/ui/button/index.js';
	import AlertDialog from '$lib/components/ui/alert-dialog/AlertDialog.svelte';
	import { userApi } from '$lib/api.js';
	import { toast } from 'svelte-sonner';
	import Cookies from 'js-cookie';
	import { goto } from '$app/navigation';
	import { cn } from '$lib/utils.js';

	let { class: className }: { class?: string } = $props();

	async function onDeleteConfirm() {
		userApi.userDelete().then(resp => {
			if (resp.status === 204) {
				toast.success('Your account has been successfully deleted.');
				Cookies.remove('auth-token');
				goto('/auth/login');
			}
		}).catch((err) => toast.error('Failed to delete your account: ' + (err.response?.data?.error ?? 'something went wrong')));
	}
</script>
<Card title="Delete your account"
			description="All data associated with your account will also be permanently deleted." class={cn("md:min-w-72", className)}>
	{#snippet content()}
		<AlertDialog title="Are you sure you want to delete your account?" description="This action is irreversible!"
								 continueOnClick={onDeleteConfirm}>
			{#snippet trigger()}
				<Button variant="destructive">Delete my account</Button>
			{/snippet}
		</AlertDialog>
	{/snippet}
</Card>