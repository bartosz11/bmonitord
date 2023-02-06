<script>
  import http from "@/http";
  import { error, info, success } from "@/toast-util";
  import { deleteCookie } from "svelte-cookie";
  import { push } from "svelte-spa-router";

  let clicks = 0;
  
  function onClick() {
    clicks++;
    if (clicks === 1) info("Are you sure?", 2000);
    if (clicks >= 2)
      http
        .delete("/api/user")
        .then((response) => {
          if (response.status == 204) {
            success("Your account has been successfully deleted.");
            deleteCookie("auth-token");
            //makes more sense than pushing to /logout i think
            push("/auth/login");
          }
        })
        .catch((err) =>
          error("Something went wrong while deleting your account.")
        );
  }
</script>

<div class="card">
  <span>Delete account (click 2 times)</span>
  <div>
    <button class="btn-danger-primary mt-3" on:click={onClick}>
      Delete account
    </button>
  </div>
</div>
