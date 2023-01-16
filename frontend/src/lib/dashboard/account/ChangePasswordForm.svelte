<script>
  import { useForm, validators, required, Hint } from "svelte-use-form";
  import toast from "svelte-french-toast";
  import { push } from "svelte-spa-router";
  import http from "@/http";

  const form = useForm();
  let oldPassword;
  let newPassword;
  let newPasswordConfirmation;

  function onSubmit(e) {
    e.preventDefault();
    http
      .patch("/api/user/password")
      .then(() => {
        toast.success("Changed password successfully.");
        push("/auth/logout");
      }).catch(err => {
        toast.error(err.response?.data?.errors[0]?.message ?? "Something went wrong while changing your password.");
      });
  }
</script>

<div class="card">
  <span>Change password</span>
  <form use:form on:submit={onSubmit}>
    <div class="my-3">
      <input
        type="password"
        bind:value={oldPassword}
        name="oldpassword"
        placeholder="Old password"
        use:validators={[required]}
        class="input-primary"
      />
      <Hint class="hint-primary" for="oldpassword" on="required">
        This field is required.
      </Hint>
    </div>

    <div class="my-3">
      <input
        type="password"
        bind:value={newPassword}
        name="newpassword"
        placeholder="New password"
        use:validators={[required]}
        class="input-primary"
      />
      <Hint class="hint-primary" for="newpassword" on="required">
        This field is required.
      </Hint>
    </div>
    <div class="my-3">
      <input
        type="password"
        bind:value={newPasswordConfirmation}
        name="newpasswordconfirm"
        placeholder="Confirm new password"
        use:validators={[required]}
        class="input-primary"
      />
      <Hint class="hint-primary" for="newpasswordconfirm" on="required">
        This field is required.
      </Hint>
    </div>
    <button disabled={!$form.valid} class="btn-ok-primary">
      Change password
    </button>
  </form>
</div>
