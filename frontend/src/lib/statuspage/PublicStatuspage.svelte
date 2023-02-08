<script>
  import http from "@/http";
  import { marked } from "marked";
  import DOMPurify from "dompurify";
  import MonitorStatusCell from "../dashboard/monitors/MonitorStatusCell.svelte";
  import StatuspageStatsRedirectButton from "./StatuspageStatsRedirectButton.svelte";
  import SvelteTable from "svelte-table";
  import { Info, Warning, WarningCircle } from "phosphor-svelte";
  export let params;

  let conditionalClasses = "text-2xl space-x-1.5";

  const fetchData = new Promise((resolve, reject) => {
    http
      .get(`/api/statuspage/${params.id}/public`)
      .then((response) => {
        switch (response.data.data.announcement?.type) { 
          case "INFO": 
            conditionalClasses += " text-sky-400";
            break;
          case "WARNING":
            conditionalClasses += " text-yellow-400";
            break;
          case "CRITICAL":
            conditionalClasses += " text-red-400";
            break;    
        }
        resolve(response.data.data);
      })
      .catch((err) => reject());
  });

  const columnConfig = [
    {
      key: "name",
      title: "Name",
      value: (v) => v.name,
      sortable: true,
    },
    {
      key: "status",
      title: "Status",
      renderComponent: MonitorStatusCell,
      sortable: true,
    },
    {
      key: "uptime",
      title: "Uptime",
      value: (v) => {
        return v.uptime + " %";
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
    {
      key: "stats",
      title: "Stats",
      renderComponent: StatuspageStatsRedirectButton,
      sortable: true,
    },
  ];
</script>

<div class="py-10 px-32">
  {#await fetchData}
    <p>Fetching data...</p>
  {:then data}
    <div class="space-y-8">
      <h1 class="text-5xl text-center">{data.name}</h1>
      {#if data.announcement !== null}
        <div class="card space-y-3">
          <div class={conditionalClasses}>
            {#if data.announcement.type === "INFO"}
              <div class="icon-align">
                <Info />
              </div>
            {:else if data.announcement.type === "WARNING"}
              <div class="icon-align">
                <Warning />
              </div>
            {:else}
              <div class="icon-align">
                <WarningCircle />
              </div>
            {/if}
            <h1 class="inline-block">{data.announcement.title}</h1>
          </div>
          {@html DOMPurify.sanitize(marked.parse(data.announcement.content))}
        </div>
      {/if}
      <div class="card">
        <h1 class="text-xl mb-5">Monitors</h1>
        <SvelteTable columns={columnConfig} rows={data.monitors} />
      </div>
    </div>
  {:catch}
    <p>Couldn't fetch data.</p>
  {/await}
</div>

<div
  class="bottom-0 fixed bg-zinc-800 w-full h-12 items-center flex flex-col justify-center"
>
  <div>
    <p>
      proudly powered by <a href="https://github.com/bartosz11/monitoring"
        ><span
          class="underline decoration-wavy underline-offset-4 decoration-sky-500"
          >bmonitord</span
        ></a
      >
    </p>
  </div>
</div>
