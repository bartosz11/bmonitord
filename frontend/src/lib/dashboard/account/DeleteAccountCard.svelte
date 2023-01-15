<script>
  import axios from "axios";
  import { deleteCookie, getCookie } from "svelte-cookie";
  import toast from "svelte-french-toast";
  import { push } from "svelte-spa-router";
  let clicks = 0;
  function onClick() {
    clicks++;
    if (clicks === 1) toast("Are you sure?", { duration: 2000 });
    if (clicks >= 2)
      axios
        .delete("/api/user", {
          headers: {
            Authorization: `Bearer ${getCookie("auth-token")}`,
          },
        })
        .then((response) => {
          if (response.status == 204) {
            toast.success("Your account has been successfully deleted.");
            deleteCookie("auth-token");
            //makes more sense than pushing to /logout i think
            push("/auth/login");
          }
        })
        .catch((err) =>
          toast.error("Something went wrong while deleting your account.")
        );
  }
</script>

<div class="card">
  <span>Delete account (click 2 times)</span>
  <div>
    <button class="btn-danger-primary mt-3" on:click={onClick}
      >Delete account</button
    >
  </div>
</div>
