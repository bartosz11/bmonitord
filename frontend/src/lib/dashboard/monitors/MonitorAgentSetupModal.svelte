<script>
  import { closeModal } from "svelte-modals";
  export let monitor;
  export let isOpen;

  const ghLink =
    "https://raw.githubusercontent.com/bartosz11/monitoring/v2/agent/agent.py";

  const formattedURL = `${window.location.origin}/api/agent/${monitor.agent.id}/post`;
</script>

{#if isOpen}
  <div role="dialog" class="modal">
    <div class="modal-contents min-w-500 space-y-4">
      <h1 class="text-xl">Agent setup instructions</h1>
      <div class="space-y-2">
        <p>
          1. Download agent from GitHub using link below. You can use wget or
          something similar.
        </p>
        <input readonly class="input-primary w-full" value={ghLink} />
        <p>
          2. Install Python 3 (minimum version: 3.11), and required modules with
          pip: py-cpuinfo requests psutil. If you're using a Linux distribution
          then you also need to install the distro module.
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
