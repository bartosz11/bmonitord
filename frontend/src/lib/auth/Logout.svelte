<script>
  import { onMount } from "svelte";
  import { deleteCookie } from "svelte-cookie";
  import { link, push } from "svelte-spa-router";
  let seconds = 5;
  onMount(() => {
    deleteCookie("auth-token");
    countdown();
  });
  function countdown() {
    setTimeout(() => {
      seconds--;
      if (seconds == 0) push("/auth/login");
      else countdown();
    }, 1000);
  }
</script>

<p>
  You've been logged out. You'll be redirected to <a
    href="/auth/login"
    use:link>login page</a
  >
  in {seconds} seconds.
</p>
