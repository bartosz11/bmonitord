<script>
  import http from "@/http";
  let down = 0;
  let up = 0;
  let paused = 0;
  let unknown = 0;
  const fetchData = new Promise((resolve, reject) => {
    http
      .get("/api/monitor")
      .then((response) => {
        response.data.data.forEach((element) => {
          if (element.paused) paused++;
          else {
            switch (element.lastStatus) {
              case "UP":
                up++;
                break;
              case "DOWN":
                down++;
                break;
              case "UNKNOWN":
                unknown++;
                break;
            }
          }
        });
        resolve();
      })
      .catch((err) => reject());
  });
</script>

{#await fetchData}
  <p>Fetching data...</p>
{:then data}
  <div class="text-center m-5 card space-y-6">
    <h1 class="text-4xl">Monitors summary</h1>
    <div class="flex flex-col md:flex-row space-x-5">
      <div class="text-green-400">
        <h1 class="text-2xl">Up</h1>
        <span class="text-4xl">{up}</span>
      </div>
      <div class="text-red-400">
        <h1 class="text-2xl">Down</h1>
        <span class="text-4xl">{down}</span>
      </div>
      <div class="text-gray-400">
        <h1 class="text-2xl">Unknown</h1>
        <span class="text-4xl">{unknown}</span>
      </div>
      <div class="text-amber-400">
        <h1 class="text-2xl">Paused</h1>
        <span class="text-4xl">{paused}</span>
      </div>
    </div>
  </div>
{:catch err}
  <p>Couldn't fetch data.</p>
{/await}
