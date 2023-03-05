<script>
  import { useForm, validators, required, Hint } from "svelte-use-form";
  import { error, success } from "@/toastUtil";
  import { push } from "svelte-spa-router";
  import http from "@/http";

  const form = useForm();
  let username;

  function onSubmit(e) {
    e.preventDefault();
    http
      .patch(`/api/user/username?username=${username}`)
      .then(() => {
        success("Changed username successfully.");
        push("/auth/logout");
      })
      .catch((err) => {
        error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while changing your username."
        );
      });
  }
</script>

<div class="card">
  <span>Change username</span>
  <form use:form on:submit={onSubmit}>
    <div class="my-3">
      <input
        type="username"
        bind:value={username}
        name="newusername"
        placeholder="New username"
        use:validators={[required]}
        class="input-primary"
      />
      <Hint class="hint-primary" for="newusername" on="required">
        This field is required.
      </Hint>
    </div>

    <button disabled={!$form.valid} class="btn-ok-primary">
      Change username
    </button>
  </form>
</div>
