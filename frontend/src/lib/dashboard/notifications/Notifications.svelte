<script>
  import http from "@/http";
  import SvelteTable from "svelte-table";
  import { openModal } from "svelte-modals";
  import Home from "@/lib/Home.svelte";
  import NotificationCreateModal from "./NotificationCreateModal.svelte";
  import NotificationActionsCell from "./NotificationActionsCell.svelte";

  const fetchData = new Promise((resolve, reject) => {
    http
      .get("/api/notification")
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
      value: (v) => {
        let uppercased = v.type.substring(0, 1).toUpperCase()+v.type.substring(1).toLowerCase();
        return uppercased.replaceAll("_", " ");
      },
      sortable: true,
    },
    {
      key: "actions",
      title: "Actions",
      renderComponent: NotificationActionsCell,
      sortable: true,
    },
  ];
</script>

{#await fetchData}
  <p>Fetching notifications...</p>
{:then data}
  <div class="flex flex-col md:flex-row">
    <button class="w-fit h-fit m-4" on:click={() => openModal(NotificationCreateModal)}>
      <i class="ph-plus-circle text-green-500 hover:text-green-600 text-3xl" />
    </button>
    <SvelteTable columns={columnSettings} rows={data} />
  </div>
{:catch}
  <p>Couldn't fetch notifications.</p>
{/await}
