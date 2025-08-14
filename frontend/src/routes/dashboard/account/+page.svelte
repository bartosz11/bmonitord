<script lang="ts">
	import { dashboardHeader } from '$lib/states';
	import UserInfoCard from '$lib/components/dashboard/account/UserInfoCard.svelte';
	import ChangeUsernameCard from '$lib/components/dashboard/account/ChangeUsernameCard.svelte';
	import ChangePasswordCard from '$lib/components/dashboard/account/ChangePasswordCard.svelte';
	import DeleteAccountCard from '$lib/components/dashboard/account/DeleteAccountCard.svelte';

	let { data } = $props();

	const user = data.user!;

	dashboardHeader.set('Account details');

</script>

<div class="flex flex-col gap-4 lg:flex-row">
		<div class="flex flex-col gap-4 2xl:flex-row">
			<UserInfoCard {user} class="2xl:h-fit" />
			<ChangeUsernameCard class="2xl:h-fit" />
		</div>
		<div class="flex flex-col gap-4 2xl:flex-row">
			<ChangePasswordCard class="2xl:h-fit" />
			<DeleteAccountCard class="2xl:h-fit" />
		</div>
</div>

