<script>
  import { useForm, validators, required, Hint } from "svelte-use-form";
  import { error, success } from "@/utils/toastUtil";
  import { push } from "svelte-spa-router";
  import http from "@/utils/httpUtil";

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

<div class="card md:col-span-2">
  <span class="text-xl font-bold">Change username</span>
  <form use:form on:submit={onSubmit} class="mt-2">
    <div class="mb-2">
      <input
        type="username"
        bind:value={username}
        name="newusername"
        placeholder="New username"
        use:validators={[required]}
        class="input-primary w-full"
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
