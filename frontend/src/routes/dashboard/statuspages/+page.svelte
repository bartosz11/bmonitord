<script lang="ts">
	import { additionalButtons, dashboardHeader } from '$lib/states';

	let { data } = $props();

	dashboardHeader.set("Your statuspages")
	additionalButtons.set(null)
</script>
