<script>
  import Router from "svelte-spa-router";
  import wrap from "svelte-spa-router/wrap";
  import { Toaster } from "svelte-french-toast";
  import { location } from "svelte-spa-router";
  import { timeout } from "./timeoutStore";
  import { getCookie } from "svelte-cookie";
  const normalRoutes = {
    "/dashboard": wrap({
      asyncComponent: () => import("./lib/dashboard/DashboardRouter.svelte"),
    }),
    "/dashboard/*": wrap({
      asyncComponent: () => import("./lib/dashboard/DashboardRouter.svelte"),
    }),
    "/report/:id": wrap({
      asyncComponent: () => import("./lib/report/MonitorStats.svelte"),
    }),
    "/statuspage/:id": wrap({
      asyncComponent: () => import("./lib/statuspage/PublicStatuspage.svelte"),
    }),
    "/auth": wrap({
      asyncComponent: () => import("./lib/auth/AuthRouter.svelte"),
    }),
    "/auth/*": wrap({
      asyncComponent: () => import("./lib/auth/AuthRouter.svelte"),
    }),
    //login page = home page
    "/": wrap({
      asyncComponent: () => import("./lib/auth/Login.svelte"),
    }),
    "*": wrap({
      asyncComponent: () => import("./lib/NotFound.svelte"),
    }),
  };
  const whiteLabelRoutes = {
    "/report/:id": wrap({
      asyncComponent: () => import("./lib/report/MonitorStats.svelte"),
    }),
    "*": wrap({
      asyncComponent: () => import("./lib/statuspage/PublicStatuspage.svelte"),
    }),
  };
  let prevValue;
  location.subscribe((v) => {
    if (prevValue !== null && prevValue !== undefined) {
      clearTimeout($timeout);
    }
    prevValue = v;
  });

  const isWhiteLabelDomain = () => {
    let cookie = getCookie("actualOrigin");
    let browserLocation = window.location.host;
    return cookie !== browserLocation;
  };
</script>

<body>
  {#if isWhiteLabelDomain()}
    <Router routes={whiteLabelRoutes} />
  {:else}
    <Router routes={normalRoutes} />
  {/if}
  <Toaster />
</body>
