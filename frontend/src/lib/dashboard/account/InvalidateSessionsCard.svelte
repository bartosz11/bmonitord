<script>
  import http from "@/http";
  import { error, success } from "@/toastUtil";
  import { deleteCookie } from "svelte-cookie";
  import { push } from "svelte-spa-router";

  function onClick() {
    http
      .post("/api/user/invalidate")
      .then((response) => {
        if (response.status == 200) {
          success("You've been logged out everywhere.");
          deleteCookie("auth-token");
          //makes more sense than pushing to /logout i think
          push("/auth/login");
        }
      })
      .catch((err) =>
        error("Something went wrong while logging you out everywhere.")
      );
  }
</script>

<div class="card">
  <span>Log out everywhere</span>
  <div>
    <button class="btn-warning-primary mt-3" on:click={onClick}>
      Log out everywhere
    </button>
  </div>
</div>
