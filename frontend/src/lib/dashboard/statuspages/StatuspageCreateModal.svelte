<script>
  import http from "@/http";
  import { error, success } from "@/toastUtil";
  import { closeModal } from "svelte-modals";
  import { Hint, required, useForm, validators } from "svelte-use-form";

  export let isOpen;
  let name;
  function onSubmit(e) {
    e.preventDefault();
    http
      .post(`/api/statuspage?name=${name}`)
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
    <div class="modal-contents">
      <form use:form on:submit={onSubmit}>
        <h1>Create statuspage</h1>
        <div>
          <input
            class="input-primary mt-4"
            type="text"
            bind:value={name}
            placeholder="Name"
            use:validators={[required]}
            name="name"
          />
          <Hint for="name" on="required" class="hint-primary"
            >This field is required.</Hint
          >
        </div>
        <div class="mt-4">
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
