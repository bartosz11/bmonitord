<script>
  import http from "@/utils/httpUtil";
  import { error } from "@/utils/toastUtil";
  const fetchData = new Promise((resolve, reject) => {
    http.get("/api/user").then((response) => {
      resolve(response.data.data);
    }).catch((err) => { 
        error(err.response?.data?.errors[0]?.message ?? "Something went wrong while fetching account data.");
        reject();
    });
  });
</script>

<div class="card md:col-span-4">
  <h2 class="font-bold text-xl">User info</h2>
  {#await fetchData}
    <p>Loading...</p>
  {:then data}
    <ul>
        <li><span class="font-bold">Username:</span> {data.username}</li>
        <li><span class="font-bold">Enabled:</span> {data.enabled}</li>
        <li><span class="font-bold">ID:</span> {data.id}</li>
    </ul>
  {:catch}
    <p>Couldn't fetch account data.</p>
  {/await}
</div>
