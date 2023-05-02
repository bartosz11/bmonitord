<script>
  import { closeModal } from "svelte-modals";

    export let onConfirm;
    export let isOpen;
</script>

{#if isOpen}
<div role="dialog" class="modal">
  <div class="modal-contents min-w-64">
    <h1 class="text-xl font-bold">
      Are you sure you want to delete your account?
    </h1>
    <div class="flex gap-2 justify-end mt-2">
        <button
          class="btn-ok-primary"
          type="button"
          on:click={() => closeModal()}>No</button
        >
        <button
          class="btn-danger-primary"
          type="button"
          on:click={() => onConfirm()}>Yes</button
        >
    </div>
  </div>
</div>
{/if}
