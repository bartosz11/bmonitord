<script lang="ts">
	import type { ComponentProps } from "svelte";
	import ArrowUpDownIcon from "@lucide/svelte/icons/arrow-up-down";
	import { Button } from '$lib/components/ui/button';

	let { variant = "ghost", innerText,  ...restProps }: ComponentProps<typeof Button> & { innerText: string } = $props();
</script>

<Button {variant} {...restProps}>
	{innerText}
	<ArrowUpDownIcon class="ml-2" />
</Button>