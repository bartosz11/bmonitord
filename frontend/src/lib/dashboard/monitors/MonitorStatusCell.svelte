<script>
  import { tooltip } from "@svelte-plugins/tooltips";
  import { ArrowCircleUp, ArrowCircleDown, Question } from "phosphor-svelte";
  export let row;
  const classesTemplate = "flex justify-center text-3xl";
  let classes;
  $: {
    classes = classesTemplate;
    if (row.paused) {
      classes += " text-amber-500";
    } else {
      switch (row.lastStatus) {
        case "UP":
          classes += " text-green-500";
          break;
        case "DOWN":
          classes += " text-red-500";
          break;
        case "UNKNOWN":
          classes += " text-gray-500";
          break;
      }
    }
  }
</script>

<div
  class={classes}
  use:tooltip={{
    content: `Last status: ${
      row.lastStatus.substring(0, 1).toUpperCase() +
      row.lastStatus.substring(1).toLowerCase()
    }<br>Paused: ${row.paused ? "Yes" : "No"}`,
    autoPosition: "true",
    position: "bottom",
  }}
>
  {#if row.lastStatus === "UP"}
    <ArrowCircleUp />
  {:else if row.lastStatus === "DOWN"}
    <ArrowCircleDown />
  {:else if row.lastStatus === "UNKNOWN"}
    <Question />
  {/if}
</div>
