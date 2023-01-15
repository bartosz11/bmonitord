<script>
    import Router from "svelte-spa-router";
    import Login from "../auth/Login.svelte";
    import Logout from "../auth/Logout.svelte";
    import Register from "../auth/Register.svelte";
    const prefix = "/auth";
    const routes = { 
        "/register": Register,
        "/logout": Logout,
        "*": Login,
    };
  </script>
  
  <Router {prefix} {routes}/>