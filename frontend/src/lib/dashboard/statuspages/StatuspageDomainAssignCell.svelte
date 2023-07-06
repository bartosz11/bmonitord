<script>
  import http from "@/utils/httpUtil";
  import { error, success } from "@/utils/toastUtil";
  import { onMount } from "svelte";

  export let row;
  export let statuspage;

  let checkValue;

  onMount(() => {
    checkValue = row.id === statuspage.whiteLabelDomain?.id;
  });

  function onClick() {
    if (!checkValue) {
      http
        .patch(`/api/statuspage/${statuspage.id}/domain/${row.id}`)
        .then((response) => {
          success("Successfully assigned domain to statuspage.");
          location.reload();
        })
        .catch((err) => {
          error(
            err.response?.data?.errors[0]?.message ??
              "Something went wrong while assigning domain to statuspage."
          );
        });
    } else {
      http
        .delete(`/api/statuspage/${statuspage.id}/domain`)
        .then((response) => {
          success("Successfully detached domain from statuspage.");
          location.reload();
        })
        .catch((err) => {
          error(
            err.response?.data?.errors[0]?.message ??
              "Something went wrong while detaching domain from statuspage."
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
