<script>
  import http from "@/http";
  import { onMount } from "svelte";
  import SvelteTable from "svelte-table";
  import { closeModal } from "svelte-modals";
  import StatuspageDomainAssignCell from "./StatuspageDomainAssignCell.svelte";
  export let row;
  export let isOpen;
  
  onMount(() => {
    delete row.$expanded;
    delete row.$selected;
    delete row.$sortOn;
  });

  const fetchData = new Promise((resolve, reject) => {
    http
      .get("/api/domain")
      .then((response) => {
        resolve(response.data.data);
      })
      .catch((err) => {
        reject();
      });
  });

  const columnSettings = [
    {
      key: "name",
      title: "Name",
      value: (v) => v.name,
      sortable: true,
      headerClass: "min-w-80"
    },
    {
      key: "assigned",
      title: "Assign to statuspage",
      renderComponent: {
        component: StatuspageDomainAssignCell,
        props: {
          statuspage: row,
        },
      },
      sortable: true,
      headerClass: "min-w-150"
    },
  ];
</script>

{#if isOpen}
  <div role="dialog" class="modal">
    <div class="modal-contents space-y-4">
      {#await fetchData}
        <p>Fetching data...</p>
      {:then data} 
        <h1>Manage domains</h1>
        <SvelteTable columns={columnSettings} rows={data}/>
        {:catch}
        <p>Couldn't fetch data.</p>
      {/await}
      <button
            class="btn-danger-primary"
            type="button"
            on:click={() => closeModal()}>Close</button
          >
    </div>
  </div>
{/if}
