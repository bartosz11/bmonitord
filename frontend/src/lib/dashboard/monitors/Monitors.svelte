<script>
  import http from "@/http";
  import { openModal } from "svelte-modals";
  import SvelteTable from "svelte-table";
  import MonitorActionsCell from "./MonitorActionsCell.svelte";
  import MonitorCreateModal from "./MonitorCreateModal.svelte";
  import MonitorStatusCell from "./MonitorStatusCell.svelte";
  import { Plus } from "phosphor-svelte";
  import { error, info } from "@/toast-util";
  import { onMount } from "svelte";
  import { timeout } from "@/timeoutStore";

  let data;

  function getData() {
    info("Fetching monitors...");
    http
      .get("/api/monitor")
      .then((response) => {
        data = response.data.data;
      })
      .catch((err) => {
        error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while fetching monitors."
        );
      });
    timeout.set(setTimeout(getData, 60000));
  }

  onMount(getData);
  
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

{#if data !== undefined}
  <div class="m-4 flex flex-row space-x-8">
    <h1 class="text-xl">Your monitors</h1>
    <button class="btn-create" on:click={() => openModal(MonitorCreateModal)}>
      <span>Create monitor</span>
      <div class="icon-align">
        <Plus />
      </div>
    </button>
  </div>
  <SvelteTable columns={columnSettings} rows={data} />
{/if}
