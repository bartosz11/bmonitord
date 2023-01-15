<script>
  import axios from "axios";
  import { deleteCookie, getCookie } from "svelte-cookie";
  import toast from "svelte-french-toast";
  import { push } from "svelte-spa-router";

  function onClick() {
    axios
      .post(
        "/api/user/invalidate",
        {},
        {
          headers: {
            Authorization: `Bearer ${getCookie("auth-token")}`,
          },
        }
      )
      .then((response) => {
        if (response.status == 200) {
          toast.success("You've been logged out everywhere.");
          deleteCookie("auth-token");
          //makes more sense than pushing to /logout i think
          push("/auth/login");
        }
      })
      .catch((err) =>
        toast.error("Something went wrong while logging you out everywhere.")
      );
  }
</script>

<div class="card">
  <span>Log out everywhere</span>
  <div>
    <button class="btn-warning-primary mt-3" on:click={onClick}>Log out everywhere</button
    >
  </div>
</div>
