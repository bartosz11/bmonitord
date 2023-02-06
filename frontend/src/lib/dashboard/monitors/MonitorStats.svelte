<script>
  import http from "@/http";
  import { Line } from "svelte-chartjs";
  import {
    cpuChart,
    diskChart,
    latencyChart,
    netChart,
    ramChart,
  } from "@/chartTemplates";
  import { onMount } from "svelte";
  import {
    Chart as ChartJS,
    Title,
    Tooltip,
    Legend,
    LineElement,
    LinearScale,
    PointElement,
    CategoryScale,
  } from "chart.js";
  import toast from "svelte-french-toast";
  import SvelteTable from "svelte-table";

  ChartJS.register(
    Title,
    Tooltip,
    Legend,
    LineElement,
    LinearScale,
    PointElement,
    CategoryScale
  );

  const incidentColSettings = [
    {
      key: "id",
      title: "ID",
      value: (v) => v.id,
      sortable: true,
    },
    {
      key: "start",
      title: "Start",
      value: (v) => {
        return new Date(v.startTimestamp).toLocaleString();
      },
      sortable: true,
    },
    {
      key: "end",
      title: "End",
      value: (v) => {
        return new Date(v.endTimestamp).toLocaleString();
      },
      sortable: true,
    },
    {
      key: "ongoing",
      title: "Ongoing",
      value: (v) => {
        return v.ongoing ? "Yes" : "No";
      },
      sortable: true,
    },
  ];
  const diskColSettings = [
    {
      key: "mountpoint",
      title: "Mountpoint",
      value: (v) => v.mountpoint,
      sortable: true,
    },
    {
      key: "usage",
      title: "Usage",
      value: (v) => {
        return v.usage + " %";
      },
      sortable: true,
    },
    {
      key: "used",
      title: "Used bytes",
      value: (v) => v.usedBytes,
      sortable: true,
    },
    {
      key: "free",
      title: "Free bytes",
      value: (v) => {
        return (v.totalBytes = v.usedBytes);
      },
      sortable: true,
    },
    {
      key: "total",
      title: "Total bytes",
      value: (v) => v.totalBytes,
      sortable: true,
    },
  ];
  export let params;
  let labels = [];
  let latencyData = [];
  let latencyChartData;
  let ramData = [];
  let swapData = [];
  let ramChartData;
  let cpuData = [];
  let iowaitData = [];
  let cpuChartData;
  let txData = [];
  let rxData = [];
  let netChartData;
  let diskData = [];
  let diskChartData;
  let lastResponse;
  let hbPage = 0;
  let monitor;
  let incidents;
  let lastIncidentResponse;
  let incidentPage = 0;
  let disksData;
  async function drawLatencyGraph() {
    http
      .get(
        `/api/heartbeat/${monitor.id}/page?size=50&page=${hbPage}&sort=timestamp,desc`
      )
      .then((resp) => {
        lastResponse = resp.data.data;
        hbPage++;
        resp.data.data.content?.forEach((element) => {
          let date = new Date(element.timestamp * 1000);
          labels.unshift([
            date.toLocaleTimeString(),
            date.toLocaleDateString(),
          ]);
          latencyData.unshift(element.responseTime);
        });
        let objCopy = Object.assign({}, latencyChart);
        objCopy.datasets[0].data = latencyData;
        objCopy.labels = labels;
        latencyChartData = objCopy;
      });
  }

  async function drawAgentGraphs() {
    http
      .get(
        `/api/heartbeat/${monitor.id}/page?size=50&page=${hbPage}&sort=timestamp,desc`
      )
      .then((resp) => {
        lastResponse = resp.data.data;
        hbPage++;
        resp.data.data.content?.forEach((element) => {
          let date = new Date(element.timestamp * 1000);
          labels.unshift([
            date.toLocaleTimeString(),
            date.toLocaleDateString(),
          ]);
          ramData.unshift(element.ramUsage);
          swapData.unshift(element.swapUsage);
          cpuData.unshift(element.cpuUsage);
          iowaitData.unshift(element.iowait);
          txData.unshift(element.tx);
          rxData.unshift(element.rx);
          diskData.unshift(element.disksUsage);
        });
        let cpuCopy = Object.assign({}, cpuChart);
        cpuCopy.datasets[0].data = cpuData;
        cpuCopy.datasets[1].data = iowaitData;
        cpuCopy.labels = labels;
        cpuChartData = cpuCopy;
        let ramCopy = Object.assign({}, ramChart);
        ramCopy.datasets[0].data = ramData;
        ramCopy.datasets[1].data = swapData;
        ramCopy.labels = labels;
        ramChartData = ramCopy;
        let netCopy = Object.assign({}, netChart);
        netCopy.datasets[0].data = rxData;
        netCopy.datasets[1].data = txData;
        netCopy.labels = labels;
        netChartData = netCopy;
        let diskCopy = Object.assign({}, diskChart);
        diskCopy.datasets[0].data = diskData;
        diskCopy.labels = labels;
        diskChartData = diskCopy;
        disksData = [];
        resp.data.data.content[0]?.diskData?.split(";").forEach((element) => {
          let diskObjUnparsed = element.split(",");
          let diskObj = {
            mountpoint: diskObjUnparsed[0],
            usage: diskObjUnparsed[1],
            totalBytes: diskObjUnparsed[2],
            usedBytes: diskObjUnparsed[3],
          };
          disksData.push(diskObj);
        });
        disksData = disksData;
      })
      .catch((err) => {});
  }

  const fetchIncidents = new Promise((resolve, reject) => {
    http
      .get(
        `/api/incident/page?id=${params.id}&page=${incidentPage}&size=5&sort=startTimestamp,desc`
      )
      .then((resp) => {
        incidents = resp.data.data.content;
        lastIncidentResponse = resp.data.data;
        resolve();
      })
      .catch((err) => {
        if (err.response?.status === 404) {
          incidents = [];
          resolve();
        } else {
          reject();
        }
      });
  });

  function getIncidents() {
    fetchIncidents;
  }

  onMount(() => {
    http
      .get(`/api/monitor/${params.id}`)
      .then((response) => {
        monitor = response.data.data;
        if (monitor.type !== "AGENT") drawLatencyGraph();
        else drawAgentGraphs();
      })
      .catch((err) => toast.error("Couldn't fetch data."));
  });
</script>

<div class="mx-4 my-6 space-y-4">
  {#if monitor !== undefined}
    <div>
      <h1 class="text-2xl">{monitor.name}</h1>
      {#if monitor.type !== "AGENT"}
        <span>{monitor.host}</span>
      {/if}
    </div>
    <div class="card w-full">
      <h1 class="text-xl">Recent incidents</h1>
      {#await fetchIncidents}
        <p>Fetching incidents...</p>
      {:then a}
        {#if incidents.length === 0}
          <p>No incidents have been found for this monitor.</p>
        {:else}
          <SvelteTable columns={incidentColSettings} rows={incidents} />
          <button
            class="btn-ok-primary"
            disabled={lastIncidentResponse?.last}
            on:click={getIncidents}>Fetch more incidents</button
          >
        {/if}
      {:catch}
        <p>Couldn't fetch incidents.</p>
      {/await}
    </div>
    {#if monitor.type === "AGENT"}
      <div class="card w-full">
        <h1 class="text-xl mb-2">Agent summary</h1>
        <div class="grid grid-rows-3 grid-flow-col">
          <div class="grid-cell">
            <div class="text-lg">CPU model</div>
            <div>{monitor.agent.cpuModel ?? "Unknown"}</div>
          </div>
          <div class="grid-cell">
            <div class="text-lg">CPU cores</div>
            <div>
              {monitor.agent.cpuCores === 0
                ? "Unknown"
                : monitor.agent.cpuCores}
            </div>
          </div>
          <!--empty-->
          <div class="grid-cell" />
          <div class="grid-cell">
            <div class="text-lg">Agent version</div>
            <div>{monitor.agent.agentVersion ?? "Unknown"}</div>
          </div>
          <div class="grid-cell">
            <div class="text-lg">Installed</div>
            <div>
              {monitor.agent.installed ? "Yes" : "No"}
            </div>
          </div>
          <div class="grid-cell">
            <div class="text-lg">Last heartbeat</div>
            <div>
              {new Date(monitor.agent.lastDataReceived * 1000).toLocaleString()}
            </div>
          </div>
          <div class="grid-cell">
            <div class="text-lg">Operating system</div>
            <div>{monitor.agent.os ?? "Unknown"}</div>
          </div>
          <div class="grid-cell">
            <div class="text-lg">Uptime (seconds)</div>
            <div>{monitor.agent.uptime}</div>
          </div>
          <div class="grid-cell">
            <div class="text-lg">IP address</div>
            <div>{monitor.agent.ipAddress ?? "Unknown"}</div>
          </div>
        </div>
      </div>
      <div class="space-y-2 card w-full">
        <h1 class="text-xl">Server stats</h1>
        {#if cpuChartData !== undefined && ramChartData !== undefined && netChartData !== undefined && diskChartData !== undefined}
          <Line data={cpuChartData} options={{ responsive: true }} />
          <Line data={ramChartData} options={{ responsive: true }} />
          <Line data={netChartData} options={{ responsive: true }} />
          <Line data={diskChartData} options={{ responsive: true }} />
          <button
            class="btn-ok-primary"
            disabled={lastResponse?.last}
            on:click={drawAgentGraphs}>Fetch more data</button
          >
        {:else}
          <p>Agent hasn't sent any stats yet.</p>
        {/if}
      </div>
      <div class="space-y-2 card w-full">
        <h1 class="text-xl">Server's disk stats</h1>
        {#if disksData !== undefined}
          <SvelteTable columns={diskColSettings} rows={disksData} />
        {:else}
          <p>Agent hasn't sent any disk stats yet.</p>
        {/if}
      </div>
    {/if}
    {#if monitor.type !== "AGENT"}
      <div class="space-y-2 card w-full">
        <h1 class="text-xl">Latency stats</h1>
        {#if latencyChartData !== undefined}
          <Line data={latencyChartData} options={{ responsive: true }} />
          <button
            class="btn-ok-primary"
            disabled={lastResponse?.last}
            on:click={drawLatencyGraph}>Fetch more data</button
          >
        {:else}
          <p>No latency data has been gathered yet.</p>
        {/if}
      </div>
    {/if}
  {/if}
</div>
