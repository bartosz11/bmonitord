<script>
  import Router, { push, location } from "svelte-spa-router";
  import wrap from "svelte-spa-router/wrap";
  import Navbar from "./Navbar.svelte";
  import { Modals, closeModal } from "svelte-modals";
  import { onMount } from "svelte";
  import { deleteCookie, getCookie } from "svelte-cookie";
  import http from "@/http";
  const prefix = "/dashboard";
  const routesAsArray = [
    "/dashboard/monitors",
    "/dashboard/notifications",
    "/dashboard/statuspages",
    "/dashboard/account",
    "/dashboard/overview",
    "/dashboard/domains",
  ];
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
    "/overview": wrap({
      asyncComponent: () => import("./Overview.svelte"),
    }),
    "/domains": wrap({
      asyncComponent: () => import("./domains/Domains.svelte"),
    }),
    "*": wrap({
      asyncComponent: () => import("../NotFound.svelte"),
    }),
  };

  onMount(() => {
    if (routesAsArray.includes($location)) {
      let cookie = getCookie("auth-token");
      //falsy values trick yes yes
      if (!cookie) push("/auth/login");
      else {
        http.get("/api/user").catch((err) => {
          deleteCookie("auth-token");
          push("/auth/login");
        });
      }
    }
  });
</script>

{#if routesAsArray.includes($location)}
  <Navbar />
{/if}
<div class="relative">
  <Router {prefix} {routes} />
</div>

<Modals>
  <!--svelte ignore is here cus idk-->
  <!-- svelte-ignore a11y-click-events-have-key-events -->
  <div slot="backdrop" class="backdrop" on:click={closeModal} />
</Modals>
