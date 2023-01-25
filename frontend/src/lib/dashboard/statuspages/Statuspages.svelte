<script>
  import http from "@/http";
  import StatuspageActionsCell from "./StatuspageActionsCell.svelte";
  import SvelteTable from "svelte-table";
  import StatuspageCreateButton from "./StatuspageCreateButton.svelte";

  const fetchData = new Promise((resolve, reject) => { 
    http.get("/api/statuspage").then(response => { 
      resolve(response.data.data);
    }).catch(err => reject());
  });

  const columnSettings = [
    {
      "key": "id",
      "title": "ID",
      "value": v => v.id,
      "sortable": true
    },
    {
      "key": "name",
      "title": "Name",
      "value": v => v.name,
      "sortable": true
    },
    {
      "key": "announcement",
      "title": "Announcement",
      "value": v => v.announcement?.title ?? "None",
      "sortable": true
    },
    {
      "key": "actions",
      "title": "Actions",
      "renderComponent": { 
        "component": StatuspageActionsCell,
        "props": {
          "id": 1
        }
      },
      "sortable": true
    }
  ]
</script>


{#await fetchData}
  <p>Fetching statuspages...</p>
{:then data} 
  <SvelteTable columns={columnSettings} rows={data}></SvelteTable>
{:catch}
<p>Couldn't fetch statuspages.</p>
{/await}