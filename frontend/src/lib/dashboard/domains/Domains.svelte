<script>
  import http from "@/http";
  import SvelteTable from "svelte-table";
  import { openModal } from "svelte-modals";
  import { Plus } from "phosphor-svelte";
  import DomainActionsCell from "./DomainActionsCell.svelte";
  import DomainCreateModal from "./DomainCreateModal.svelte";

  let statuspages;

  const fetchStatuspages = new Promise((resolve, reject) => {
    http
      .get("/api/statuspage")
      .then((response) => {
        statuspages = response.data.data;
        resolve();
      })
      .catch((err) => reject());
  });

  const fetchDomains = new Promise((resolve, reject) => {
    http
      .get("/api/domain")
      .then((response) => {
        resolve(response.data.data);
      })
      .catch((err) => reject());
  });
  
  const fetchData = Promise.all([fetchDomains, fetchStatuspages]);

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
      key: "domain",
      title: "Domain",
      value: (v) => v.domain,
      sortable: true,
    },
    {
      key: "statuspage",
      title: "Statuspage",
      value: (v) => {
        return (
          statuspages.filter((page) => page.whiteLabelDomain?.id === v.id)[0]
            ?.name ?? "None"
        );
      },
      sortable: true,
    },
    {
      key: "actions",
      title: "Actions",
      renderComponent: DomainActionsCell,
      sortable: true,
    },
  ];
</script>

{#await fetchData}
  <p>Fetching data required to render this page...</p>
{:then data}
  <div class="m-4 flex flex-row space-x-8">
    <h1 class="text-xl">Your white label domains</h1>
    <button class="btn-create" on:click={() => openModal(DomainCreateModal)}>
      <span>Create white label domain</span>
      <div class="icon-align">
        <Plus />
      </div>
    </button>
  </div>
  <SvelteTable columns={columnSettings} rows={data[0]} />
{:catch}
  <p>Couldn't fetch data required to render this page.</p>
{/await}
