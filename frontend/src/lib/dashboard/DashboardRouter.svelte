<script>
  import Router from "svelte-spa-router";
  import Account from "./account/Account.svelte";
  import Monitors from "./Monitors.svelte";
  import Navbar from "./Navbar.svelte";
  import Notifications from "./Notifications.svelte";
  import Overview from "./Overview.svelte";
  import Statuspages from "./Statuspages.svelte";
  const prefix = "/dashboard";
  const routes = { 
    "/monitors": Monitors,
    "/notifications": Notifications,
    "/statuspages": Statuspages,
    "/account": Account,
    "*": Overview
  };
</script>

<Navbar/>
<div class="h-view">
  <Router {prefix} {routes}/>
</div>