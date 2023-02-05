<script>
  import { onMount } from "svelte";
  import { getCookie } from "svelte-cookie";
  import { link, push } from "svelte-spa-router";
  import { Hint, required, useForm, validators } from "svelte-use-form";
  import toast from "svelte-french-toast";
  import http from "@/http";

  const form = useForm();
  let username;
  let password;

  const onSubmit = (e) => {
    e.preventDefault();
    http
      .post("/api/auth/login", {
        username,
        password,
      })
      .then((response) => {
        push("/dashboard/overview");
        toast.success("Logged in successfully.");
      })
      .catch((err) => {
        toast.error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while logging in."
        );
      });
  };

  onMount(() => {
    let cookie = getCookie("auth-token");
    if (cookie !== "") {
      push("/dashboard/overview");
    }
  });
</script>

<div class="grid place-items-center h-screen text-center">
  <div class="space-y-4">
    <h1 class="text-xl">Log in to bmonitord</h1>
    <form use:form on:submit={onSubmit} class="space-y-3">
      <div>
        <input
          name="username"
          placeholder="Username"
          type="text"
          class="input-primary"
          use:validators={[required]}
          bind:value={username}
        />
        <Hint class="hint-primary" for="username" on="required">
          This field is required.
        </Hint>
      </div>
      <div>
        <input
          name="password"
          placeholder="Password"
          type="password"
          class="input-primary"
          use:validators={[required]}
          bind:value={password}
        />
        <Hint class="hint-primary" for="password" on="required">
          This field is required.
        </Hint>
      </div>
      <button disabled={!$form.valid} class="btn-ok-primary">Log in</button>
    </form>
    <div>
      <a href="/auth/register" use:link>Don't have an account yet? <span class="underline decoration-wavy underline-offset-4 decoration-sky-500">Register</span></a>
    </div>
  </div>
</div>
