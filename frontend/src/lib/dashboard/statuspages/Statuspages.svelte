<script>
  import http from "@/http";
  import StatuspageActionsCell from "./StatuspageActionsCell.svelte";
  import SvelteTable from "svelte-table";
  import { openModal } from "svelte-modals";
  import StatuspageCreateModal from "./StatuspageCreateModal.svelte";

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
  <div class="flex flex-col md:flex-row">
    <button class="w-fit h-fit m-4" on:click={() => openModal(StatuspageCreateModal)}>
      <i class="ph-plus-circle text-green-500 hover:text-green-600 text-3xl" />
    </button>
    <SvelteTable columns={columnSettings} rows={data} />
  </div>
{:catch}
  <p>Couldn't fetch statuspages.</p>
{/await}
