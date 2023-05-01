<script>
  import { getCookie } from "svelte-cookie";
  import { closeModal } from "svelte-modals";
  export let domain;
  export let isOpen;
  let actualOrigin = getCookie("actualOrigin") || window.location.host;
</script>

{#if isOpen}
  <div role="dialog" class="modal">
    <div class="modal-contents min-w-500 space-y-4">
      <h1 class="text-xl">White label domain setup instructions</h1>
      <div class="space-y-2">
        <p>
          1. Add a DNS record for <b>{domain.domain}</b> pointing to <b>{actualOrigin}</b>
        </p>
        <p>
          2. Make sure your proxies can proxy connections from <b>{domain.domain}</b> if you have them set up.
        </p>
        <p>
          3. Assign this domain to a statuspage.
        </p>
      </div>
      <div>
        <button
          class="btn-danger-primary"
          type="button"
          on:click={() => closeModal()}>Close</button
        >
      </div>
    </div>
  </div>
{/if}
