<script>
  import { onMount } from "svelte";
  import { getCookie, setCookie } from "svelte-cookie";
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
      .post("/api/auth/register", {
        username,
        password,
      })
      .then((response) => {
        if (response.status === 201) {
          http
            .post("/api/auth/login", {
              username,
              password,
            })
            .then((response) => {
              setCookie("auth-token", response.data.token, 1, true);
              push("/dashboard/overview");
              toast.success("Registered and logged in successfully.");
            })
            .catch((err) => {
              toast.error(
                err?.response?.data?.errors[0]?.message ?? "Something went wrong while logging in."
              );
            });
        }
      })
      .catch((err) => {
        if (err.response && err.response.data.errors) {
          toast.error(err.response.data.errors[0].message);
        } else toast.error("Something went wrong while registering your account.");
      });
  }
  onMount(() => {
    let cookie = getCookie("auth-token");
    if (cookie !== "") {
      push("/dashboard/overview");
    }
  });
</script>

<h1>Register</h1>
<form use:form on:submit={onSubmit}>
  <div class="mt-3">
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
  <div class="my-3">
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
<a href="/auth/login" use:link>Have an account already?</a>
