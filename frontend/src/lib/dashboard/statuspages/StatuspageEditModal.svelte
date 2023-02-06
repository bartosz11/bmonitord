<script>
  import http from "@/http";
  import { error, success } from "@/toast-util";
  import { closeModal } from "svelte-modals";
  import { Hint, required, useForm, validators } from "svelte-use-form";

  export let isOpen;
  export let id;
  let newName;
  function onSubmit(e) {
    e.preventDefault();
    http
      .patch(`/api/statuspage/${id}/rename`, {
        name: newName,
      })
      .then((response) => {
        success("Successfully renamed statuspage.");
        //once again im too lazy to change all the data
        location.reload();
      })
      .catch((err) => {
        error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while renaming statuspage."
        );
      });
  }
  const form = useForm();
</script>

{#if isOpen}
  <div role="dialog" class="modal">
    <div class="modal-contents">
      <form use:form on:submit={onSubmit}>
        <h1>Rename statuspage</h1>
        <div>
          <input
            class="input-primary mt-4"
            type="text"
            bind:value={newName}
            placeholder="New name"
            use:validators={[required]}
            name="newname"
          />
          <Hint for="newname" on="required" class="hint-primary"
            >This field is required.</Hint
          >
        </div>
        <div class="mt-4">
          <!--button type is specified to stop closing modal on enter-->
          <button class="btn-danger-primary" type="button" on:click={() => closeModal()}
            >Cancel</button
          >
          <button class="btn-ok-primary" disabled={!$form.valid}
            >Rename</button
          >
        </div>
      </form>
    </div>
  </div>
{/if}
