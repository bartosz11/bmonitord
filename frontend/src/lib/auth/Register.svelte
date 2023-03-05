<script>
  import { onMount } from "svelte";
  import { getCookie } from "svelte-cookie";
  import { link, push } from "svelte-spa-router";
  import { Hint, required, useForm, validators } from "svelte-use-form";
  import { success, error } from "@/toastUtil";
  import http from "@/http";

  const form = useForm();
  let username;
  let password;

  const onSubmit = (e) => {
    e.preventDefault();
    http
      .post("/api/auth/register", {
        username,
        password,
      })
      .then((response) => {
        if (response.status === 201) {
          success("Registered successfully.");
          push("/auth/login");
        }
      })
      .catch((err) => {
        if (err.response && err.response.data.errors) {
          error(err.response.data.errors[0].message);
        } else
          error("Something went wrong while creating your account.");
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
    <h1 class="text-xl">Register</h1>
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
        <Hint class="hint-primary" for="Username" on="required">
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
      <button disabled={!$form.valid} class="btn-ok-primary">Register</button>
    </form>
    <div>
      <a href="/auth/login" use:link >Have an account already? <span class="underline decoration-wavy underline-offset-4 decoration-sky-500">Log in</span></a>
    </div>
  </div>
</div>
