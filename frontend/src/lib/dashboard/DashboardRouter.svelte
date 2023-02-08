<script>
  import Router from "svelte-spa-router";
  import wrap from "svelte-spa-router/wrap";
  import Navbar from "./Navbar.svelte";
  import { Modals, closeModal } from "svelte-modals";
  const prefix = "/dashboard";
  const routes = {
    "/monitors": wrap({
      asyncComponent: () => import("./monitors/Monitors.svelte"),
    }),
    "/notifications": wrap({
      asyncComponent: () => import("./notifications/Notifications.svelte"),
    }),
    "/statuspages": wrap({
      asyncComponent: () => import("./statuspages/Statuspages.svelte"),
    }),
    "/account": wrap({
      asyncComponent: () => import("./account/Account.svelte"),
    }),
    "*": wrap({
      asyncComponent: () => import("./Overview.svelte"),
    }),
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
