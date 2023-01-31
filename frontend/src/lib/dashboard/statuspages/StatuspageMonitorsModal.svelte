<script>
    import SvelteTable from "svelte-table";
    import { closeModal } from "svelte-modals";
    export let statuspage;
    export let isOpen;
  
    const columnSettings = [
      {
        key: "id",
        title: "ID",
        value: (v) => v.id,
        sortable: true,
      },
      {
        key: "name",
        title: "Name",
        value: (v) => v.name,
        sortable: true,
      },
      {
        key: "lastStatus",
        title: "Last status",
        value: (v) => {
          return (
            v.lastStatus.substring(0, 1).toUpperCase() +
            v.lastStatus.substring(1).toLowerCase()
          );
        },
        sortable: true,
      },
      {
        key: "lastCheck",
        title: "Last check",
        value: (v) => {
          return new Date(v.lastCheck * 1000).toLocaleString();
        },
        sortable: true,
      },
    ];
  </script>
  
  {#if isOpen}
    <div role="dialog" class="modal">
      <div class="modal-contents space-y-4">
        <h1>Monitors assigned to this statuspage</h1>
        {#if statuspage.monitors.length === 0}
          <p>There are no monitors added to this statuspage.</p>
        {:else}
          <SvelteTable columns={columnSettings} rows={statuspage.monitors} />
        {/if}
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
  