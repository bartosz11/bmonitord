<script>
  import Router from "svelte-spa-router";
  import wrap from "svelte-spa-router/wrap";
  const prefix = "/auth";
  const routes = {
    "/register": wrap({
      asyncComponent: () => import("./Register.svelte"),
    }),
    "/logout": wrap({
      asyncComponent: () => import("./Logout.svelte"),
    }),
    "/login": wrap({
      asyncComponent: () => import("./Login.svelte"),
    }),
    "*": wrap({
      asyncComponent: () => import("../NotFound.svelte"),
    }),
  };
</script>

<Router {prefix} {routes} />
