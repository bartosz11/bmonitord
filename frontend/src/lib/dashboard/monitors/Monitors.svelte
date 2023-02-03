<script>
  import http from "@/http";
  import { openModal } from "svelte-modals";
  import SvelteTable from "svelte-table";
  import MonitorActionsCell from "./MonitorActionsCell.svelte";
  import MonitorCreateModal from "./MonitorCreateModal.svelte";
  import MonitorStatusCell from "./MonitorStatusCell.svelte";
  import { tooltip } from "@svelte-plugins/tooltips";
  import { PlusCircle } from "phosphor-svelte";
  const fetchData = new Promise((resolve, reject) => {
    http
      .get("/api/monitor")
      .then((response) => {
        resolve(response.data.data);
      })
      .catch((err) => reject());
  });

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
      key: "type",
      title: "Type",
      value: (v) => v.type,
      sortable: true,
    },
    {
      key: "uptime",
      title: "Uptime percent",
      value: (v) => {
        return v.uptimePercent + " %";
      },
      sortable: true,
    },
    {
      key: "status",
      title: "Status",
      sortable: true,
      renderComponent: MonitorStatusCell,
    },
    {
      key: "lastCheck",
      title: "Last check",
      value: (v) => {
        return new Date(v.lastCheck * 1000).toLocaleString();
      },
      sortable: true,
    },
    {
      key: "actions",
      title: "Actions",
      renderComponent: MonitorActionsCell,
      sortable: true,
    },
  ];
</script>

{#await fetchData}
  <p>Fetching monitors...</p>
{:then data}
  <div class="flex flex-col md:flex-row">
    <button
      class="w-fit h-fit m-4 text-green-500 hover:text-green-600 text-3xl"
      on:click={() => openModal(MonitorCreateModal)}
      use:tooltip={{
        content: "Create monitor",
        autoPosition: "true",
        position: "right",
      }}
    >
      <PlusCircle />
    </button>
    <SvelteTable columns={columnSettings} rows={data} />
  </div>
{:catch}
  <p>Couldn't fetch monitors.</p>
{/await}
