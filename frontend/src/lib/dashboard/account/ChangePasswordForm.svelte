<script>
  import { useForm, validators, required, Hint } from "svelte-use-form";
  import { error, success } from "@/utils/toastUtil";
  import { push } from "svelte-spa-router";
  import http from "@/utils/httpUtil";

  const form = useForm();
  let oldPassword;
  let newPassword;
  let newPasswordConfirmation;

  function onSubmit(e) {
    e.preventDefault();
    http
      .patch("/api/user/password", {
        oldPassword: oldPassword,
        newPassword: newPassword,
        newPasswordConfirmation: newPasswordConfirmation,
      })
      .then(() => {
        success("Changed password successfully.");
        push("/auth/logout");
      })
      .catch((err) => {
        error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while changing your password."
        );
      });
  }
</script>

<div class="card !h-full md:row-span-2 md:col-span-2 flex items-center">
  <div class="w-full">
    <span class="font-bold text-xl">Change password</span>
    <form use:form on:submit={onSubmit} class="flex flex-col gap-y-3 mt-2">
      <div class="w-full">
        <input
          type="password"
          bind:value={oldPassword}
          name="oldpassword"
          placeholder="Old password"
          use:validators={[required]}
          class="input-primary w-full"
        />
        <Hint class="hint-primary" for="oldpassword" on="required">
          This field is required.
        </Hint>
      </div>
  
      <div>
        <input
          type="password"
          bind:value={newPassword}
          name="newpassword"
          placeholder="New password"
          use:validators={[required]}
          class="input-primary w-full"
        />
        <Hint class="hint-primary" for="newpassword" on="required">
          This field is required.
        </Hint>
      </div>
      <div>
        <input
          type="password"
          bind:value={newPasswordConfirmation}
          name="newpasswordconfirm"
          placeholder="Confirm new password"
          use:validators={[required]}
          class="input-primary w-full"
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
</div>
