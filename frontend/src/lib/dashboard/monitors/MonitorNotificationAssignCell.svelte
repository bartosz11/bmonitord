<script>
  import http from "@/http";
  import { onMount } from "svelte";
  import toast from "svelte-french-toast";

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
          toast.success("Successfully assigned notification to monitor.");
        })
        .catch((err) => {
          toast.error(
            err.response?.data?.errors[0]?.message ??
              "Something went wrong while assigning notification to monitor."
          );
        });
    } else { 
        http
        .delete(`/api/monitor/${monitor.id}/notification/${row.id}`)
        .then((response) => {
          toast.success("Successfully deallocated notification from monitor.");
        })
        .catch((err) => {
          toast.error(
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
