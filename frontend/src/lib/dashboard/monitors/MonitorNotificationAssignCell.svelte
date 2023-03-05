<script>
  import http from "@/http";
  import { error, success } from "@/toastUtil";
  import { onMount } from "svelte";

  export let row;
  export let monitor;

  let checkValue;

  onMount(() => {
    let check = row.monitors?.some(m => { 
        return m.id === monitor.id;
    })
    if (check==true) checkValue = true;
    else checkValue = false;
  });

  function onClick() {
    if (!checkValue) {
      http
        .patch(`/api/monitor/${monitor.id}/notification/${row.id}`)
        .then((response) => {
          success("Successfully assigned notification to monitor.");
        })
        .catch((err) => {
          error(
            err.response?.data?.errors[0]?.message ??
              "Something went wrong while assigning notification to monitor."
          );
        });
    } else { 
        http
        .delete(`/api/monitor/${monitor.id}/notification/${row.id}`)
        .then((response) => {
          success("Successfully deallocated notification from monitor.");
        })
        .catch((err) => {
          error(
            err.response?.data?.errors[0]?.message ??
              "Something went wrong while deallocating notification from monitor."
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
