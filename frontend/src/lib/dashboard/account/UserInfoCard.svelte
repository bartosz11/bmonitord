<script>
  import http from "@/http";
  import { error } from "@/toastUtil";
  const fetchData = new Promise((resolve, reject) => {
    http.get("/api/user").then((response) => {
      resolve(response.data.data);
    }).catch((err) => { 
        error(err.response?.data?.errors[0]?.message ?? "Something went wrong while fetching account data.");
        reject();
    });
  });
</script>

<div class="card">
  <p>User info</p>
  {#await fetchData}
    <p>Loading...</p>
  {:then data}
    <ul>
        <li>Username: {data.username}</li>
        <li>Enabled: {data.enabled}</li>
        <li>ID: {data.id}</li>
    </ul>
  {:catch}
    <p>Couldn't fetch account data.</p>
  {/await}
</div>
