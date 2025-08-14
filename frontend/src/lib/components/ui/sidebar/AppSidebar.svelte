<script lang="ts">
	import * as Sidebar from '$lib/components/ui/sidebar/index.js';
	import Tooltip from '../tooltip/Tooltip.svelte';
	import {
		ActivityIcon,
		BellIcon,
		CogIcon,
		ListChecksIcon,
		RadarIcon,
		SettingsIcon,
		CircleUserRound,
		KeyRoundIcon
	} from '@lucide/svelte';
	import type { ModelUser } from '$lib/api-client-axios';
	import SidebarLogoutButton from './SidebarLogoutButton.svelte';

	let { user }: SidebarProps = $props();

	type SidebarProps = {
		user: ModelUser;
	};

	const userItems = [
		{
			title: 'Targets',
			url: '/dashboard/targets',
			icon: ActivityIcon
		},
		{
			title: 'Notifications',
			url: '/dashboard/notifications',
			icon: BellIcon
		},
		{
			title: 'Statuspages',
			url: '/dashboard/statuspages',
			icon: ListChecksIcon
		}
	];

	const adminItems = [
		{
			title: 'Checkers',
			url: '/dashboard/admin/checkers',
			icon: RadarIcon
		},
		{
			title: 'Orchestrators',
			url: '/dashboard/admin/orchestrators',
			icon: CogIcon
		},
		{
			title: 'Instance settings',
			url: '/dashboard/admin/settings',
			icon: SettingsIcon
		}
	];

	const accountItems = [
		{
			title: 'Settings',
			url: '/dashboard/account',
			icon: SettingsIcon
		},
		{
			title: 'Login sessions',
			url: '/dashboard/sessions',
			icon: KeyRoundIcon
		}
	];
</script>

<Sidebar.Root>
	<Sidebar.Header>
		<h1 class="mt-2 text-center text-xl font-semibold">checkmate</h1>
	</Sidebar.Header>
	<Sidebar.Content>
		<Sidebar.Group>
			<Sidebar.GroupLabel>Personal</Sidebar.GroupLabel>
			<Sidebar.GroupContent>
				<Sidebar.Menu>
					{#each userItems as item (item)}
						<Sidebar.MenuItem>
							<Sidebar.MenuButton>
								{#snippet child({ props })}
									<a href={item.url} {...props}>
										<item.icon />
										<span>{item.title}</span>
									</a>
								{/snippet}
							</Sidebar.MenuButton>
						</Sidebar.MenuItem>
					{/each}
				</Sidebar.Menu>
			</Sidebar.GroupContent>
		</Sidebar.Group>

		{#if user.admin}
			<Sidebar.Group>
				<Sidebar.GroupLabel>Management</Sidebar.GroupLabel>
				<Sidebar.GroupContent>
					<Sidebar.Menu>
						{#each adminItems as item (item)}
							<Sidebar.MenuItem>
								<Sidebar.MenuButton>
									{#snippet child({ props })}
										<a href={item.url} {...props}>
											<item.icon />
											<span>{item.title}</span>
										</a>
									{/snippet}
								</Sidebar.MenuButton>
							</Sidebar.MenuItem>
						{/each}
					</Sidebar.Menu>
				</Sidebar.GroupContent>
			</Sidebar.Group>
		{/if}
		<Sidebar.Group class="mt-auto">
			<Sidebar.GroupLabel>Account</Sidebar.GroupLabel>
			<Sidebar.GroupContent>
				<Sidebar.Menu>
					{#each accountItems as item (item)}
						<Sidebar.MenuItem>
							<Sidebar.MenuButton>
								{#snippet child({ props })}
									<a href={item.url} {...props}>
										<item.icon />
										<span>{item.title}</span>
									</a>
								{/snippet}
							</Sidebar.MenuButton>
						</Sidebar.MenuItem>
					{/each}
				</Sidebar.Menu>
			</Sidebar.GroupContent>
		</Sidebar.Group>
	</Sidebar.Content>
	<Sidebar.Footer class="my-2">
		<div class="grid grid-cols-4 grid-rows-1 gap-2 px-1">
			<div class="col-start col-span-3 row-span-1 flex flex-row items-center gap-2">
				<CircleUserRound size={36} />
				<span>
					{user.username}
				</span>
			</div>
			<Tooltip content="Log out" class="col-4 ml-2">
				{#snippet trigger()}
					<SidebarLogoutButton />
				{/snippet}
			</Tooltip>
		</div>
	</Sidebar.Footer>
</Sidebar.Root>
