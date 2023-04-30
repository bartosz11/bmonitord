<script>
  let menuOpen = false;
  import { List, X } from "phosphor-svelte";
  import { push } from "svelte-spa-router";
  import active from "svelte-spa-router/active";

  function toggleMenu() {
    menuOpen = !menuOpen;
  }
</script>

<div class="bg-zinc-800 sticky">
  <div class="container mx-auto">
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
            <button class="nav-element" on:click={() => push("/dashboard/monitors")} use:active={"/dashboard/monitors"}>Monitors</button>
            <button class="nav-element" on:click={() => push("/dashboard/notifications")} use:active={"/dashboard/notifications"}>Notifications</button>
            <button class="nav-element" on:click={() => push("/dashboard/statuspages")} use:active={"/dashboard/statuspages"}>Statuspages</button>
            <button class="nav-element" on:click={() => push("/dashboard/domains")} use:active={"/dashboard/domains"}>Domains</button>
            <button class="nav-element" on:click={() => push("/dashboard/account")} use:active={"/dashboard/account"}>Account</button>
            <button class="nav-element" on:click={() => push("/auth/logout")}>Log out</button>
        </nav>
      </div>
    </div>
    <div class="md:hidden {menuOpen ? 'block' : 'hidden'}">
      <nav>
        <div class="nav-element-mobile">
          <button on:click={() => push("/dashboard/monitors")} >Monitors</button>
        </div>
        <div class="nav-element-mobile">
          <button on:click={() => push("/dashboard/notifications")}>Notifications</button>
        </div>
        <div class="nav-element-mobile">
          <button on:click={() => push("/dashboard/statuspages")}>Statuspages</button>
        </div>
        <div class="nav-element-mobile">
          <button on:click={() => push("/dashboard/domains")}>Domains</button>
        </div>
        <div class="nav-element-mobile">
          <button on:click={() => push("/dashboard/account")}>Account</button>
        </div>
        <div class="nav-element-mobile">
          <button on:click={() => push("/auth/logout")}>Log out</button>
        </div>
      </nav>
    </div>
  </div>
</div>
