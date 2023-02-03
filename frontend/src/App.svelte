<script>
  import Router from "svelte-spa-router";
  import wrap from "svelte-spa-router/wrap";
  import { Toaster } from "svelte-french-toast";
  const routes = {
    "/dashboard": wrap({
      asyncComponent: () => import("./lib/dashboard/DashboardRouter.svelte"),
    }),
    "/dashboard/*": wrap({
      asyncComponent: () => import("./lib/dashboard/DashboardRouter.svelte"),
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
</script>

<body>
  <Router {routes} />
  <Toaster />
</body>
