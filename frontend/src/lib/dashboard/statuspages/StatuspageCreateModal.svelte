<script>
  import http from "@/utils/httpUtil";
  import { error, success } from "@/utils/toastUtil";
  import { closeModal } from "svelte-modals";
  import { Hint, required, useForm, validators } from "svelte-use-form";

  export let isOpen;
  let name;
  let logoLink;
  let logoRedirect;
  function onSubmit(e) {
    e.preventDefault();
    http
      .post(`/api/statuspage`, {
        name: name,
        logoLink: logoLink,
        logoRedirect: logoRedirect,
        //optional in the API, might implement this on frontend at some point
        monitorIds: []
      })
      .then((response) => {
        success("Successfully created a new statuspage.");
        location.reload();
      })
      .catch((err) => {
        error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while creating statuspage."
        );
      });
  }
  const form = useForm();
</script>

{#if isOpen}
  <div role="dialog" class="modal">
    <div class="modal-contents w-64">
      <form use:form on:submit={onSubmit} class="space-y-4">
        <h1>Create statuspage</h1>
        <div>
          <input
            class="input-primary w-full"
            type="text"
            bind:value={name}
            use:validators={[required]}
            placeholder="Name"
            name="name"
          />
          <Hint for="name" class="hint-primary" on="required">
            This field is required.
          </Hint>
        </div>
        <div>
          <input
            type="text"
            name="logolink"
            placeholder="Logo URL (optional)"
            bind:value={logoLink}
            class="input-primary w-full"
          />
        </div>
        <div>
          <input
            type="text"
            name="logoredirect"
            placeholder="Logo redirect URL (optional)"
            class="input-primary w-full"
            bind:value={logoRedirect}
          />
        </div>
        <div>
          <!--button type is specified to stop closing modal on enter-->
          <button
            class="btn-danger-primary"
            type="button"
            on:click={() => closeModal()}>Cancel</button
          >
          <button class="btn-ok-primary" disabled={!$form.valid}>Create</button>
        </div>
      </form>
    </div>
  </div>
{/if}