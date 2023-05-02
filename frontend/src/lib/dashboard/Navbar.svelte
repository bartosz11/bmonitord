<script>
  let menuOpen = false;
  import { List, X } from "phosphor-svelte";
  import { link, push } from "svelte-spa-router";
  import active from "svelte-spa-router/active";

  function toggleMenu() {
    menuOpen = !menuOpen;
  }
</script>

<div class="bg-zinc-800 sticky">
  <div class="px-4 lg:px-0 container-lg mx-auto">
    <div class="flex justify-between items-center py-2">
      <a href="#/dashboard/overview" class="text-white text-xl font-semibold">bmonitord</a>
      <div class="flex md:hidden">
        <button class="text-white text-xl" on:click={toggleMenu}>
          {#if menuOpen}
            <X />
          {:else}
            <List />
          {/if}
        </button>
      </div>
      <div class="hidden md:flex">
        <nav>
            <a class="nav-element" href={"/dashboard/monitors"} use:link use:active={"/dashboard/monitors"}>Monitors</a>
            <a class="nav-element" href={"/dashboard/notifications"} use:link use:active={"/dashboard/notifications"}>Notifications</a>
            <a class="nav-element" href={"/dashboard/statuspages"} use:link use:active={"/dashboard/statuspages"}>Statuspages</a>
            <a class="nav-element" href={"/dashboard/domains"} use:link use:active={"/dashboard/domains"}>Domains</a>
            <a class="nav-element" href={"/dashboard/account"} use:link use:active={"/dashboard/account"}>Account</a>
            <a class="nav-element break-none whitespace-nowrap" href={"/auth/logout"} use:link>Log out</a>
        </nav>
      </div>
    </div>
    <div class="md:hidden {menuOpen ? 'block' : 'hidden'} pb-2">
      <nav>
        <a class="nav-element-mobile" href={"/dashboard/monitors"} use:link use:active={"/dashboard/monitors"}>Monitors</a>
        <a class="nav-element-mobile" href={"/dashboard/notifications"} use:link use:active={"/dashboard/notifications"}>Notifications</a>
        <a class="nav-element-mobile" href={"/dashboard/statuspages"} use:link use:active={"/dashboard/statuspages"}>Statuspages</a>
        <a class="nav-element-mobile" href={"/dashboard/domains"} use:link use:active={"/dashboard/domains"}>Domains</a>
        <a class="nav-element-mobile" href={"/dashboard/account"} use:link use:active={"/dashboard/account"}>Account</a>
        <a class="nav-element-mobile" href={"/auth/logout"} use:link>Log out</a>
      </nav>
    </div>
  </div>
</div>
