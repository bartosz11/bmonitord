<script>
  import { push } from "svelte-spa-router";
</script>

<nav
  class="sticky md:w-screen md:h-nav bg-zinc-800 text-base flex flex-col md:flex-row"
>
  <div class="nav-element">
    <button on:click={() => push("/dashboard/overview")}>bmonitord</button>
  </div>
  <div class="nav-element">
    <button on:click={() => push("/dashboard/monitors")}>Monitors</button>
  </div>
  <div class="nav-element">
    <button on:click={() => push("/dashboard/notifications")}
      >Notifications</button
    >
  </div>
  <div class="nav-element">
    <button on:click={() => push("/dashboard/statuspages")}>Statuspages</button>
  </div>
  <div class="nav-element">
    <button on:click={() => push("/dashboard/account")}>Account</button>
  </div>
  <div class="nav-element">
    <button on:click={() => push("/auth/logout")}>Log out</button>
  </div>
</nav>
