<script>
  import Router from "svelte-spa-router";
  import wrap from "svelte-spa-router/wrap";
  import { Toaster } from "svelte-french-toast";
  import { location } from "svelte-spa-router";
  import { timeout } from "./timeoutStore";
  const routes = {
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
    "*": wrap({
      asyncComponent: () => import("./lib/Home.svelte"),
    }),
  };

  let prevValue;
  location.subscribe((v) => {
    if (prevValue !== null && prevValue !== undefined) {
      clearTimeout($timeout);
    }
    prevValue = v;
  });
</script>

<body>
  <Router {routes} />
  <Toaster />
</body>
