<script lang="ts">
    import {onMount} from 'svelte';
    import type {ModelIncident} from '$lib/api-client-axios';
    import UptimeTimelineTick from '$lib/components/report/UptimeTimelineTick.svelte';
    import {SvelteDate} from 'svelte/reactivity';

    type UptimeTimelineProps = {
        incidents: ModelIncident[],
        pastDays: number
    }

    let {incidents, pastDays}: UptimeTimelineProps = $props();

    const rangeEnd = new SvelteDate(); // now
    const rangeStart = new SvelteDate(rangeEnd);
    rangeStart.setDate(rangeEnd.getDate() - pastDays); // make the date "x days ago"

    let container: HTMLElement;
    let width = $state(0);
    const ro = new ResizeObserver(([entry]) => {
        width = entry.contentRect.width;
    });

    onMount(() => {
        ro.observe(container);
        return () => ro.disconnect();
    });

    const pxPerTick = 12; // 8px of the tick itself, 2px of margin on both left and right = 12
    let tickCount = $derived(Math.floor(width / pxPerTick));

    let bucketDuration = $derived((rangeEnd.getTime() - rangeStart.getTime()) / tickCount); // ms
</script>


<div class="w-full flex justify-center" bind:this={container}>
    <div class="flex gap-1">
        <!-- eslint-disable-next-line @typescript-eslint/no-unused-vars How else am I supposed to indicate that a variable is not used, /shrug-->
        {#each Array(tickCount) as _, i (i)}
            <UptimeTimelineTick {incidents}
                                bucketStart={new SvelteDate(rangeStart.getTime() + i * bucketDuration)}
                                bucketEnd={new SvelteDate(rangeStart.getTime() + (i + 1) * bucketDuration)} index={i}/>
        {/each}
    </div>
</div>
<div class="mt-1 flex w-full justify-between">
    <span class="text-sm">{pastDays}d ago</span>
    <span class="text-sm">now</span>
</div>