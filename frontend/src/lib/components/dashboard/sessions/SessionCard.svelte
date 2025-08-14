<script lang="ts">
	import type { ModelSession } from '$lib/api-client-axios/index.js';
	import Card from '$lib/components/ui/card/Card.svelte';
	import Cookies from 'js-cookie';
	import { Badge } from '$lib/components/ui/badge';
	import { LogOutIcon } from '@lucide/svelte';
	import { Button } from '$lib/components/ui/button';
	import Tooltip from '$lib/components/ui/tooltip/Tooltip.svelte';
	import dayjs from 'dayjs';
	import relativeTime from 'dayjs/plugin/relativeTime.js';
	import { sessionApi } from '$lib/api';
	import { toast } from 'svelte-sonner';
	import { goto } from '$app/navigation';

	let { session }: { session: ModelSession } = $props();

	dayjs.extend(relativeTime);

	let destroyed = $state(false);
	// if there's browser and os data display that nicely, otherwise display UA, if that's absent - blank string
	let cardTitle = (session.browser && session.os) ? session.browser + ' on ' + session.os : session.userAgent ?? '';
	// I hate how the svelte templating doesn't allow TS asserts
	let dates = {
		createdAt: session.createdAt!,
		expiresAt: session.expiresAt!,
		lastActive: session.lastActive!
	};

	function isValid() {
		if (new Date(session.expiresAt!) < new Date()) return false;
		return new Date(session.createdAt!) >= new Date(session.user!.createdAt!);
	}

	function isCurrentSession() {
		// has to exist if this page / component even loads
		const authToken = Cookies.get('auth-token')!;
		const payload = authToken.split('.')[1];
		const payloadObj = JSON.parse(atob(payload));
		return payloadObj.sessionID === session.id;
	}

	async function onLogoutClick() {
		let id = session.id!;
		sessionApi.sessionIdDelete(id).then(resp => {
			if (resp.status === 204) {
				if (isCurrentSession()) {
					Cookies.remove('auth-token');
					goto('/auth/login');
				} else {
					destroyed = true;
				}
				toast.success('Successfully logged out the session used by ' + cardTitle + '.');
			}
		}).catch((err) => toast.error('Failed to log out session: ' + (err.response?.data?.error ?? 'something went wrong')));
	}
</script>

{#if !destroyed}
	<Card>
		{#snippet title()}
			<div>
				<h1>{cardTitle}</h1>
				<div class="mt-2">
					{#if isValid()}
						<Badge variant="outline" class="bg-green-900 ">Valid</Badge>
					{:else}
						<Badge variant="outline" class="bg-red-900 ">Invalid</Badge>
					{/if}
					{#if isCurrentSession()}
						<Badge variant="secondary" class="bg-blue-900">Current session</Badge>
					{/if}
				</div>
			</div>
		{/snippet}
		{#snippet action()}
			<Tooltip content="Log out (invalidate) this session">
				{#snippet trigger()}
					<Button variant="outline" onclick={onLogoutClick}>
						<LogOutIcon />
					</Button>
				{/snippet}
			</Tooltip>
		{/snippet}
		{#snippet content()}
			<p>Device: {session.device ?? "Unknown" } — IP: {session.ipAddress}</p>
			<p>
				<Tooltip content={new Date(dates.createdAt).toLocaleString()}>
					{#snippet trigger()}
						<span>Created: {dayjs(dates.createdAt).fromNow()}</span>
					{/snippet}
				</Tooltip> &bull;
				<Tooltip content={new Date(dates.lastActive).toLocaleString()}>
					{#snippet trigger()}
						<span> Last active: {dayjs(dates.lastActive).fromNow()}</span>
					{/snippet}
				</Tooltip>
				&bull;
				<Tooltip content={new Date(dates.expiresAt).toLocaleString()}>
					{#snippet trigger()}
						<span>Expires: {dayjs(session.expiresAt).fromNow()}</span>
					{/snippet}
				</Tooltip>
			</p>
		{/snippet}
	</Card>
{/if}