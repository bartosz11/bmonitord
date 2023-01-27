<script>
  //this modal is meant to allow the user to see what monitor caused this notification to go brrt or smth
  //basically give info about monitor checks nothing else
  import SvelteTable from "svelte-table";
  import { closeModal } from "svelte-modals";
  export let notification;
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
      <h1>Monitors using this notification</h1>
      {#if notification.monitors.length === 0}
        <p>There are no monitors using this notification.</p>
      {:else}
        <SvelteTable columns={columnSettings} rows={notification.monitors} />
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
