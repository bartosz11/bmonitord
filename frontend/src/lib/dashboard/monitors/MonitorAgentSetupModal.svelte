<script>
  export let monitor;
  export let isOpen;
</script>

{#if isOpen}
  <div role="dialog" class="modal">
    <div class="modal-contents">
      <h1>agent setup</h1>
      <span>id: {monitor.agent?.id}</span>
    </div>
  </div>
{/if}