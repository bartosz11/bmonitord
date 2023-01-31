<script>
  import { push } from "svelte-spa-router";
  import active from "svelte-spa-router/active";
</script>

<nav class="sticky md:min-w-screen md:h-nav bg-zinc-800 text-base flex flex-col md:flex-row">
  <div class="nav-element" use:active={"/dashboard/overview"}>
    <button on:click={() => push("/dashboard/overview")}>bmonitord</button>
  </div>
  <div class="nav-element" use:active={"/dashboard/monitors"}>
    <button on:click={() => push("/dashboard/monitors")}>Monitors</button>
  </div>
  <div class="nav-element" use:active={"/dashboard/notifications"}>
    <button on:click={() => push("/dashboard/notifications")}>Notifications</button>
  </div>
  <div class="nav-element" use:active={"/dashboard/statuspages"}>
    <button on:click={() => push("/dashboard/statuspages")}>Statuspages</button>
  </div>
  <div class="nav-element" use:active={"/dashboard/account"}>
    <button on:click={() => push("/dashboard/account")}>Account</button>
  </div>
  <div class="nav-element">
    <button on:click={() => push("/auth/logout")}>Log out</button>
  </div>
</nav>
