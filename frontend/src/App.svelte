<script>
  import Router from "svelte-spa-router";
  import AuthRouter from "./lib/auth/AuthRouter.svelte";
  import Dashboard from "./lib/dashboard/DashboardRouter.svelte";
  import Home from "./lib/Home.svelte";
  import { Toaster } from "svelte-french-toast";
  const routes = {
    "/dashboard": Dashboard,
    "/dashboard/*": Dashboard,
    "/auth": AuthRouter,
    "/auth/*": AuthRouter,
    "*": Home,
  };
</script>

<body>
  <Router {routes} />
  <Toaster />
</body>
