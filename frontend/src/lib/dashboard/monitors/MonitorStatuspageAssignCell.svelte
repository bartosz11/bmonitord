<script>
  import http from "@/http";
  import { onMount } from "svelte";
  import toast from "svelte-french-toast";

  export let row;
  export let monitor;

  let checkValue;

  onMount(() => {
    let check = row.monitors?.some((m) => {
      return JSON.stringify(m) === JSON.stringify(monitor);
    });
    if (check==true) checkValue = true;
    else checkValue = false;
  });

  function onClick() {
    if (!checkValue) {
      http
        .patch(`/api/monitor/${monitor.id}/statuspage/${row.id}`)
        .then((response) => {
          toast.success("Successfully added monitor to statuspage.");
        })
        .catch((err) => {
          toast.error(
            err.response?.data?.errors[0]?.message ??
              "Something went wrong while adding monitor to statuspage."
          );
        });
    } else {
      http
        .delete(`/api/monitor/${monitor.id}/statuspage/${row.id}`)
        .then((response) => {
          toast.success("Successfully removed monitor from statuspage.");
        })
        .catch((err) => {
          toast.error(
            err.response?.data?.errors[0]?.message ??
              "Something went wrong while removing monitor from statuspage."
          );
        });
    }
  }
</script>

<div>
  <input
    on:click={onClick}
    bind:checked={checkValue}
    class="input-primary"
    type="checkbox"
    name="assigned"
  />
</div>
