<script>
  import http from "@/http";
  import { error, success } from "@/toastUtil";
  import { closeModal } from "svelte-modals";
  import { Hint, required, useForm, validators } from "svelte-use-form";
  export let isOpen;
  export let announcement;
  export let statuspageId;
  let title = announcement?.title ?? undefined;
  let content = announcement?.content ?? undefined;
  let type = announcement?.type ?? undefined;
  const form = useForm();

  function onSubmit(e) {
    e.preventDefault();
    http
      .post(`/api/statuspage/${statuspageId}/announcement`, {
        title: title,
        content: content,
        type: type,
      })
      .then((response) => {
        success("Successfully saved announcement.");
        location.reload();
      })
      .catch((err) => {
        error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while saving announcement."
        );
      });
  }

  function deleteAnnouncement() {
    http
      .delete(`/api/statuspage/${statuspageId}/announcement`)
      .then((response) => {
        success("Successfully removed announcement.");
        location.reload();
      })
      .catch((err) => {
        error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while removing announcement."
        );
      });
  }
</script>

{#if isOpen}
  <div role="dialog" class="modal">
    <div class="modal-contents">
      <form use:form on:submit={onSubmit}>
        <h1>Edit statuspage announcement</h1>
        <div>
          <input
            class="input-primary my-4 w-full"
            type="text"
            bind:value={title}
            placeholder="Title"
            use:validators={[required]}
            name="title"
          />
          <Hint for="title" on="required" class="hint-primary"
            >This field is required.</Hint
          >
        </div>
        <div>
          <label for="type">Type</label>
          <select
            name="type"
            class="input-primary"
            placeholder="Select type"
            bind:value={type}
            use:validators={[required]}
          >
            <option value="INFO">Information</option>
            <option value="WARNING">Warning</option>
            <option value="CRITICAL">Critical</option>
          </select>
          <Hint for="type" on="required" class="hint-primary"
            >This field is required.</Hint
          >
        </div>
        <div>
          <textarea
            class="input-primary my-4 w-full h-32 resize-none"
            bind:value={content}
            placeholder="Content (you can use markdown)"
          />
        </div>
        <div class="mt-4">
          <button
            class="btn-danger-primary"
            type="button"
            on:click={() => closeModal()}>Cancel</button
          >
          <button
            class="btn-danger-primary"
            disabled={announcement === null}
            type="button"
            on:click={deleteAnnouncement}>Remove announcement</button
          >
          <button class="btn-ok-primary" disabled={!$form.valid}
            >Save announcement</button
          >
        </div>
      </form>
    </div>
  </div>
{/if}
