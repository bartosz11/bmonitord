<script>
  import { onMount } from "svelte";
  import { getCookie, setCookie } from "svelte-cookie";
  import { link, push } from "svelte-spa-router";
  import { Hint, required, useForm, validators } from "svelte-use-form";
  import axios from "axios";
  import toast from "svelte-french-toast";
  const form = useForm();
  let username;
  let password;
  function onSubmit(e) {
    e.preventDefault();
    axios
      .post("/api/auth/login", {
        username: username,
        password: password,
      })
      .then((response) => {
        setCookie("auth-token", response.data.token, 1, true);
        push("/dashboard/overview");
        toast.success("Logged in successfully.");
      })
      .catch((err) => { 
        if (err.response && err.response.data.errors) {
          toast.error(err.response.data.errors[0].message);
        } else toast.error("Something went wrong while logging in.");
      });
  }
  onMount(() => {
    let cookie = getCookie("auth-token");
    if (cookie !== "") {
      push("/dashboard/overview");
    }
  });
</script>

<h1>Login</h1>
<form use:form on:submit={onSubmit}>
  <div class="mt-3">
    <input
      name="Username"
      placeholder="Username"
      type="text"
      class="input-primary"
      use:validators={[required]}
      bind:value={username}
    />
    <Hint class="hint-primary" for="Username" on="required"
      >This field is required.</Hint
    >
  </div>
  <div class="my-3">
    <input
      name="Password"
      placeholder="Password"
      type="password"
      class="input-primary"
      use:validators={[required]}
      bind:value={password}
    />
    <Hint class="hint-primary" for="Password" on="required"
      >This field is required.</Hint
    >
  </div>
  <button disabled={!$form.valid} class="btn-ok-primary">Log in</button>
</form>
<a href="/auth/register" use:link>Don't have an account yet?</a>
