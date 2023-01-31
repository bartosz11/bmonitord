<script>
    import http from "@/http";
    import { closeModal } from "svelte-modals";
    import { Hint, required, useForm, validators } from "svelte-use-form";
    import toast from "svelte-french-toast";
  
    export let isOpen;
    export let id;
    let newName;
    function onSubmit(e) {
      e.preventDefault();
      http
        .patch(`/api/monitor/${id}/rename`, {
          name: newName,
        })
        .then((response) => {
          toast.success("Successfully renamed monitor.");
          location.reload();
        })
        .catch((err) => {
          toast.error(
            err.response?.data?.errors[0]?.message ??
              "Something went wrong while renaming monitor."
          );
        });
    }
    const form = useForm();
  </script>
  
  {#if isOpen}
    <div role="dialog" class="modal">
      <div class="modal-contents">
        <form use:form on:submit={onSubmit}>
          <h1>Rename monitor</h1>
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
  