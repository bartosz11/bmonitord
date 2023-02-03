<script>
  import http from "@/http";
  import SvelteTable from "svelte-table";
  import { openModal } from "svelte-modals";
  import NotificationCreateModal from "./NotificationCreateModal.svelte";
  import NotificationActionsCell from "./NotificationActionsCell.svelte";
  import { Plus } from "phosphor-svelte";

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
        let uppercased =
          v.type.substring(0, 1).toUpperCase() +
          v.type.substring(1).toLowerCase();
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
  <div class="m-4 flex flex-row space-x-8">
    <h1 class="text-xl">Your notifications</h1>
    <button
      class="btn-create"
      on:click={() => openModal(NotificationCreateModal)}
    >
      <span>Create notification</span>
      <div class="icon-align">
        <Plus />
      </div>
    </button>
  </div>
  <SvelteTable columns={columnSettings} rows={data} />
{:catch}
  <p>Couldn't fetch notifications.</p>
{/await}
