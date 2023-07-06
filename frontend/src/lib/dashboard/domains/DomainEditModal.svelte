<script>
  import http from "@/utils/httpUtil";
  import { error, success } from "@/utils/toastUtil";
  import { closeModal } from "svelte-modals";
  import { Hint, required, useForm, validators } from "svelte-use-form";

  export let isOpen;
  export let domain;
  //technically these options aren't optional buuuuuut
  let name = domain.name ?? undefined;
  let domainName = domain.domain ?? undefined;
  function onSubmit(e) {
    e.preventDefault();
    http
      .patch(`/api/domain/${domain.id}`, {
        name: name,
        domain: domainName,
      })
      .then((response) => {
        success("Successfully edited white label domain.");
        location.reload();
      })
      .catch((err) => {
        error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while editing white label domain."
        );
      });
  }
  const form = useForm();
</script>

{#if isOpen}
  <div role="dialog" class="modal">
    <div class="modal-contents w-64">
      <form use:form on:submit={onSubmit} class="space-y-4">
        <h1>Edit white label domain</h1>
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
            bind:value={domainName}
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
          <button class="btn-ok-primary" disabled={!$form.valid}>Edit</button>
        </div>
      </form>
    </div>
  </div>
{/if}
