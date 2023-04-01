<script>
  import { tooltip } from "@svelte-plugins/tooltips";
  import { ArrowUpRight } from "phosphor-svelte";
  export let row;

  function onClick() {
    window.open(`#/report/${row.id}`, "_blank").focus();
  }
</script>

<div class="m-2">
  <button
    on:click={onClick}
    class="border border-blue-500 p-1 w-fit h-fit text-blue-500 hover:bg-blue-500 hover:text-white text-xl"
    use:tooltip={{
      content: "Open monitor stats in another tab",
      autoPosition: "true",
      position: "bottom",
    }}
  >
    <ArrowUpRight />
  </button>
</div>
