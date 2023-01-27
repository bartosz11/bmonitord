<script>
  import Router, { push } from "svelte-spa-router";
  import Account from "./account/Account.svelte";
  import Monitors from "./Monitors.svelte";
  import Navbar from "./Navbar.svelte";
  import Notifications from "./notifications/Notifications.svelte";
  import Overview from "./Overview.svelte";
  import Statuspages from "./statuspages/Statuspages.svelte";
  import {Modals, closeModal } from "svelte-modals";
  import { onMount } from "svelte";
  import http from "@/http";
  import { deleteCookie } from "svelte-cookie";
  const prefix = "/dashboard";
  const routes = { 
    "/monitors": Monitors,
    "/notifications": Notifications,
    "/statuspages": Statuspages,
    "/account": Account,
    "*": Overview
  };
  onMount(() => { 
    http.get("/api/user").catch(err => { 
      deleteCookie("auth-token");
      push("/auth/login");
    })
  })
</script>

<Navbar/>
<div class="h-view">
  <Router {prefix} {routes}/>
</div>

<Modals>
  <!--svelte ignore is here cus idk-->
  <!-- svelte-ignore a11y-click-events-have-key-events -->
  <div
    slot="backdrop"
    class="backdrop"
    on:click={closeModal}
  />
</Modals>