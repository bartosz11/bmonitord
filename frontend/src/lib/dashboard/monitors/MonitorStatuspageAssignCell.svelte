<script>
  import http from "@/http";
  import { error, success } from "@/toastUtil";
  import { onMount } from "svelte";

  export let row;
  export let monitor;

  let checkValue;

  onMount(() => {
    checkValue = row.monitors?.some((m) => {
      return m.id === monitor.id;
    });
  });

  function onClick() {
    if (!checkValue) {
      http
        .patch(`/api/monitor/${monitor.id}/statuspage/${row.id}`)
        .then((response) => {
          success("Successfully added monitor to statuspage.");
        })
        .catch((err) => {
          error(
            err.response?.data?.errors[0]?.message ??
              "Something went wrong while adding monitor to statuspage."
          );
        });
    } else {
      http
        .delete(`/api/monitor/${monitor.id}/statuspage/${row.id}`)
        .then((response) => {
          success("Successfully removed monitor from statuspage.");
        })
        .catch((err) => {
          error(
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
