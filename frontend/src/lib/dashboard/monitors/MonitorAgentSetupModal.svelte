<script>
  import http from "@/http";
  import { closeModal } from "svelte-modals";
  export let monitor;
  export let isOpen;

  const ghLink =
    "https://raw.githubusercontent.com/bartosz11/monitoring/v2/agent/agent.py";
    
  const fetchAgent = new Promise((resolve, reject) => {
    http
      .get(`/api/monitor/${monitor.id}/agent`)
      .then((response) => {
        let formattedURL = `${window.location.origin}/api/agent/${response.data.data.id}/post`;
        resolve(formattedURL);
      })
      .catch((err) => reject());
  });
</script>

{#if isOpen}
  <div role="dialog" class="modal">
    <div class="modal-contents min-w-500 space-y-4">
      <h1 class="text-xl">Agent setup instructions</h1>
      {#await fetchAgent}
        <p>Fetching data required to display instructions...</p>
      {:then formattedURL}
        <div class="space-y-2">
          <p>
            1. Download agent from GitHub using link below. You can use wget or
            something similar.
          </p>
          <input readonly class="input-primary w-full" value={ghLink} />
          <p>
            2. Install Python 3 (minimum version: 3.11), and required modules
            with pip: py-cpuinfo requests psutil. If you're using a Linux distribution then you also need to install the distro module.
          </p>
          <p>
            3. Open the agent file with your favourite text editor and change
            value of the URL variable to:
          </p>
          <input readonly class="input-primary w-full" value={formattedURL} />
          <p>
            4. Add the agent to cron or Windows's Task Scheduler to run every
            minute.
          </p>
        </div>
      {:catch err}
        <p>Something went wrong while fetching data.</p>
      {/await}
      <div>
        <button
          class="btn-danger-primary"
          type="button"
          on:click={() => closeModal()}>Close</button
        >
      </div>
    </div>
  </div>
{/if}
