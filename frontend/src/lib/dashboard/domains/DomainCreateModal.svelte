<script>
  import http from "@/http";
  import { error, success } from "@/toastUtil";
  import { closeModal } from "svelte-modals";
  import { Hint, required, useForm, validators } from "svelte-use-form";

  export let isOpen;
  let name;
  let domain;
  function onSubmit(e) {
    e.preventDefault();
    http
      .post(`/api/domain`, {
        name: name,
        domain: domain,
      })
      .then((response) => {
        success("Successfully created a new white label domain.");
        location.reload();
      })
      .catch((err) => {
        error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while creating white label domain."
        );
      });
  }
  const form = useForm();
</script>

{#if isOpen}
  <div role="dialog" class="modal">
    <div class="modal-contents w-64">
      <form use:form on:submit={onSubmit} class="space-y-4">
        <h1>Create white label domain</h1>
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
            name="domain"
            placeholder="Domain"
            bind:value={domain}
            use:validators={[required]}
            class="input-primary w-full"
          />
          <Hint for="domain" class="hint-primary" on="required">
            This field is required.
          </Hint>
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
