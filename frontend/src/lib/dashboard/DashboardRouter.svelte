<script>
  import Router from "svelte-spa-router";
  import Account from "./account/Account.svelte";
  import Monitors from "./monitors/Monitors.svelte";
  import Navbar from "./Navbar.svelte";
  import Notifications from "./notifications/Notifications.svelte";
  import Overview from "./Overview.svelte";
  import Statuspages from "./statuspages/Statuspages.svelte";
  import { Modals, closeModal } from "svelte-modals";
  import MonitorStats from "./monitors/MonitorStats.svelte";
  const prefix = "/dashboard";
  const routes = {
    "/monitors": Monitors,
    "/monitors/:id": MonitorStats,
    "/notifications": Notifications,
    "/statuspages": Statuspages,
    "/account": Account,
    "*": Overview,
  };
</script>

<Navbar />
<div class="h-view relative">
  <Router {prefix} {routes} />
</div>

<Modals>
  <!--svelte ignore is here cus idk-->
  <!-- svelte-ignore a11y-click-events-have-key-events -->
  <div slot="backdrop" class="backdrop" on:click={closeModal} />
</Modals>
