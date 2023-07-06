<script>
  import http from "@/utils/httpUtil";
  import StatuspageActionsCell from "./StatuspageActionsCell.svelte";
  import SvelteTable from "svelte-table";
  import { openModal } from "svelte-modals";
  import StatuspageCreateModal from "./StatuspageCreateModal.svelte";
  import { Plus } from "phosphor-svelte";

  const fetchData = new Promise((resolve, reject) => {
    http
      .get("/api/statuspage")
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
      key: "announcement",
      title: "Announcement",
      value: (v) => v.announcement?.title ?? "None",
      sortable: true,
    },
    {
      key: "domain",
      title: "Domain",
      value: (v) => v.whiteLabelDomain?.name ?? "None",
      sortable: true,
    },
    {
      key: "actions",
      title: "Actions",
      renderComponent: StatuspageActionsCell,
      sortable: true,
    },
  ];
</script>

{#await fetchData}
  <p>Fetching statuspages...</p>
{:then data}
  <div class="m-4 flex flex-row space-x-8">
    <h1 class="text-xl">Your statuspages</h1>
    <button
      class="btn-create"
      on:click={() => openModal(StatuspageCreateModal)}
    >
      <span>Create statuspage</span>
      <div class="icon-align">
        <Plus />
      </div>
    </button>
  </div>
  <SvelteTable columns={columnSettings} rows={data} />
{:catch}
  <p>Couldn't fetch statuspages.</p>
{/await}
