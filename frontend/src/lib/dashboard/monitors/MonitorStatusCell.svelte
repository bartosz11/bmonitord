<script>
  import { tooltip } from "@svelte-plugins/tooltips";
  export let row;
  let classes = "text-3xl";
  switch (row.lastStatus) {
    case "UP":
      classes += " ph-arrow-circle-up";
      if (!row.paused) classes += " text-green-500";
      break;
    case "DOWN":
      classes += " ph-arrow-circle-down";
      if (!row.paused) classes += " text-red-500";
      break;
    case "UNKNOWN":
      classes += " ph-question";
      if (!row.paused) classes += " text-gray-500";
      break;
  }
  if (row.paused) {
    classes += " text-amber-500";
  }
</script>

<i
  class={classes}
  use:tooltip={{
    content: `Last status: ${row.lastStatus.substring(0,1).toUpperCase()+row.lastStatus.substring(1).toLowerCase()}<br>Paused: ${row.paused ? "Yes" : "No"}`,
    autoPosition: "true",
    position: "bottom",
  }}
/>
