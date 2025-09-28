<script lang="ts">
	import { additionalButtons, dashboardHeader } from '$lib/states';
	import TargetDataForm from '$lib/components/dashboard/targets/TargetDataForm.svelte';

	let { data } = $props();

	dashboardHeader.set(`Edit ${data.target.name}`);
	additionalButtons.set(null);
</script>

<TargetDataForm target={data.target} locations={data.checkers}/>