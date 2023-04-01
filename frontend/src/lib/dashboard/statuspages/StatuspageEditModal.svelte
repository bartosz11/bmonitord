<script>
  import http from "@/http";
  import { error, success } from "@/toastUtil";
  import { closeModal } from "svelte-modals";
  import { useForm } from "svelte-use-form";

  export let isOpen;
  export let statuspage;
  let name = statuspage.name;
  let logoLink = statuspage.logoLink;
  let logoRedirect = statuspage.logoRedirect;
  function onSubmit(e) {
    e.preventDefault();
    http
      .patch(`/api/statuspage/${statuspage.id}`, {
        name: name,
        logoLink: logoLink,
        logoRedirect: logoRedirect,
      })
      .then((response) => {
        success("Successfully edited statuspage.");
        //once again im too lazy to change all the data
        location.reload();
      })
      .catch((err) => {
        error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while editing statuspage."
        );
      });
  }
  const form = useForm();
</script>

{#if isOpen}
  <div role="dialog" class="modal">
    <div class="modal-contents w-64">
      <form use:form on:submit={onSubmit} class="space-y-4">
        <h1>Edit statuspage</h1>
        <div>
          <input
            class="input-primary w-full"
            type="text"
            bind:value={name}
            placeholder="New name"
            name="newname"
          />
        </div>
        <div>
          <input
            type="text"
            name="logolink"
            placeholder="Logo URL"
            bind:value={logoLink}
            class="input-primary w-full (optional)"
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
          <button class="btn-ok-primary" disabled={!$form.valid}>Edit</button>
        </div>
      </form>
    </div>
  </div>
{/if}
