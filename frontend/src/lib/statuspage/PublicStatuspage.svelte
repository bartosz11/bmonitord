<script>
  import http from "@/http";
  import { timeout } from "@/timeoutStore";
  import { promise } from "@/toastUtil";
  import DOMPurify from "dompurify";
  import { marked } from "marked";
  import { Info, Warning, WarningCircle } from "phosphor-svelte";
  import { onMount } from "svelte";
  import SvelteTable from "svelte-table";
  import MonitorStatusCell from "../dashboard/monitors/MonitorStatusCell.svelte";
  import StatuspageStatsRedirectButton from "./StatuspageStatsRedirectButton.svelte";
  export let params;
  let conditionalClasses;
  let data;
  let id = params.id ?? window.location.host;

  const fetcher = async () => {
    const response = await http.get(`/api/statuspage/${id}/public`)
    switch (response.data.data.announcement?.type) {
      case "INFO":
        conditionalClasses = "text-2xl space-x-1.5 text-sky-400";
        break;
      case "WARNING":
        conditionalClasses = "text-2xl space-x-1.5 text-yellow-400";
        break;
      case "CRITICAL":
        conditionalClasses = "text-2xl space-x-1.5 text-red-400";
        break;
    }
    data = response.data.data;
  }

  function getData() {
    promise(
      fetcher(),
      {
        loading: 'Fetching data...',
        error: 'Failed to fetch data. Retrying in 1 minute.',
        success: null
      }).catch(() => {
      timeout.set(setTimeout(getData, 60000));
    })
  }

  onMount(getData);

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

<div class="min-h-screen flex flex-col justify-between">
  <div class="py-10 px-32 mb-auto">
    <div class="space-y-8">
      {#if data !== undefined}
        <div class="grid place-items-center space-y-12">
          {#if data.logoLink !== null}
            <a href={data.logoRedirect ?? "javascript:void(0);"}>
              <img src={data.logoLink} alt="Logo" />
            </a>
          {/if}
          <h1 class="text-5xl text-center">{data.name}</h1>
        </div>
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
      {/if}
    </div>
  </div>

  <div
    class="bottom-0 bg-zinc-800 w-full h-12 items-center flex flex-col justify-center"
  >
    <div>
      <p>
        proudly powered by <a href="https://github.com/bartosz11/bmonitord"
          ><span
            class="underline decoration-wavy underline-offset-4 decoration-sky-500"
            >bmonitord</span
          ></a
        >
      </p>
    </div>
  </div>
</div>
