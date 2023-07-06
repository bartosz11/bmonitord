<script>
  import http from "@/http";
  import { openModal } from "svelte-modals";
  import { push } from "svelte-spa-router";
  import MonitorAgentSetupModal from "./MonitorAgentSetupModal.svelte";
  import MonitorNotificationsModal from "./MonitorNotificationsModal.svelte";
  import MonitorRenameModal from "./MonitorRenameModal.svelte";
  import MonitorStatuspagesModal from "./MonitorStatuspagesModal.svelte";
  import { tooltip } from "@svelte-plugins/tooltips";
  import {
    Trash,
    Pencil,
    Play,
    Pause,
    ShareNetwork,
    BellRinging,
    ListChecks,
    Info,
    Heartbeat,
    Sunglasses,
  } from "phosphor-svelte";
  import { error, promise, success } from "@/toastUtil";
  import ConfirmationModal from "@/lib/ConfirmationModal.svelte";
  export let row;

  function onDeleteClick() {
    openModal(ConfirmationModal, {
      title: `Are you sure you want to delete ${row.name}?`,
      onConfirm: () => {
        promise(http.delete(`/api/monitor/${row.id}`), {
          success: `Successfully deleted ${row.name}!`,
          error: null,
          loading: "Deleting monitor...",
        })
          .then(() => location.reload())
          .catch((err) =>
            error(
              err.response?.data?.errors[0]?.message ??
                "Something went wrong while deleting monitor."
            )
          );
      },
    });
  }

  function onEditClick() {
    openModal(MonitorRenameModal, { id: row.id });
  }

  function onPauseClick() {
    http
      .patch(`/api/monitor/${row.id}/pause?pause=${!row.paused}`)
      .then((response) => {
        location.reload();
        success(
          `Successfully changed pause state for monitor with ID ${row.id}`
        );
      })
      .catch((err) =>
        error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while changing pause state for monitor."
        )
      );
  }

  function onPublishClick() {
    http
      .patch(`/api/monitor/${row.id}/publish?public=${!row.published}`)
      .then((response) => {
        location.reload();
        success(
          `Successfully changed published state for monitor with ID ${row.id}.`
        );
      })
      .catch((err) =>
        error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while changing published state for monitor."
        )
      );
  }

  function onHideIPClick() {
    http
      .patch(`/api/monitor/${row.id}/agentIP?hide=${!row.agent.hideIP}`)
      .then((response) => {
        location.reload();
        success(
          `Successfully toggled IP visibility for monitor with ID ${row.id}.`
        );
      })
      .catch((err) =>
        error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while toggling IP visibility for monitor."
        )
      );
  }

  function onNotificationsClick() {
    openModal(MonitorNotificationsModal, { row: row });
  }

  function onStatuspagesClick() {
    openModal(MonitorStatuspagesModal, { row: row });
  }

  function onInfoClick() {
    push(`/report/${row.id}`);
  }
</script>

<div class="flex flex-row space-x-2 m-2">
  <!--delete-->
  <button
    on:click={onDeleteClick}
    class="border border-red-500 p-1 w-fit h-fit text-red-500 hover:bg-red-500 hover:text-white text-xl"
    use:tooltip={{
      content: "Delete monitor",
      autoPosition: "true",
      position: "bottom",
    }}
  >
    <Trash />
  </button>
  <!--edit-->
  <button
    on:click={onEditClick}
    class="border border-blue-500 p-1 w-fit h-fit text-blue-500 hover:bg-blue-500 hover:text-white text-xl"
    use:tooltip={{
      content: "Rename monitor",
      autoPosition: "true",
      position: "bottom",
    }}
  >
    <Pencil />
  </button>
  <!--pause-->
  {#if row.paused}
    <button
      on:click={onPauseClick}
      class="border border-green-500 p-1 w-fit h-fit text-green-500 hover:bg-green-500 hover:text-white text-xl"
      use:tooltip={{
        content: "Resume monitor",
        autoPosition: "true",
        position: "bottom",
      }}
    >
      <Play />
    </button>
  {:else}
    <button
      on:click={onPauseClick}
      class="border border-yellow-500 p-1 w-fit h-fit text-yellow-500 hover:bg-yellow-500 hover:text-white text-xl"
      use:tooltip={{
        content: "Pause monitor",
        autoPosition: "true",
        position: "bottom",
      }}
    >
      <Pause />
    </button>
  {/if}
  <!--publish-->
  {#if row.published}
    <button
      on:click={onPublishClick}
      class="border border-red-500 p-1 w-fit h-fit text-red-500 hover:bg-red-500 hover:text-white text-xl"
      use:tooltip={{
        content: "Make monitor stats private",
        autoPosition: "true",
        position: "bottom",
      }}
    >
      <ShareNetwork />
    </button>
  {:else}
    <button
      on:click={onPublishClick}
      class="border border-green-500 p-1 w-fit h-fit text-green-500 hover:bg-green-500 hover:text-white text-xl"
      use:tooltip={{
        content: "Make monitor stats public",
        autoPosition: "true",
        position: "bottom",
      }}
    >
      <ShareNetwork />
    </button>
  {/if}
  <!--notifications-->
  <button
    on:click={onNotificationsClick}
    class="border border-blue-500 p-1 w-fit h-fit text-blue-500 hover:bg-blue-500 hover:text-white text-xl"
    use:tooltip={{
      content: "Manage monitors notifications",
      autoPosition: "true",
      position: "bottom",
    }}
  >
    <BellRinging />
  </button>
  <!--statuspages-->
  <button
    on:click={onStatuspagesClick}
    class="border border-blue-500 p-1 w-fit h-fit text-blue-500 hover:bg-blue-500 hover:text-white text-xl"
    use:tooltip={{
      content: "Manage monitors statuspages",
      autoPosition: "true",
      position: "bottom",
    }}
  >
    <ListChecks />
  </button>
  <!--info-->
  <button
    on:click={onInfoClick}
    class="border border-blue-500 p-1 w-fit h-fit text-blue-500 hover:bg-blue-500 hover:text-white text-xl"
    use:tooltip={{
      content: "Monitor stats",
      autoPosition: "true",
      position: "bottom",
    }}
  >
    <Info />
  </button>
  <!--agent setup-->
  {#if row.type === "AGENT"}
    <button
      on:click={() => {
        openModal(MonitorAgentSetupModal, { monitor: row });
      }}
      class="border border-blue-500 p-1 w-fit h-fit text-blue-500 hover:bg-blue-500 hover:text-white text-xl"
      use:tooltip={{
        content: "Set up Server Agent",
        autoPosition: "true",
        position: "bottom",
      }}
    >
      <Heartbeat />
    </button>
    <button
      on:click={onHideIPClick}
      class="border border-blue-500 p-1 w-fit h-fit text-blue-500 hover:bg-blue-500 hover:text-white text-xl"
      use:tooltip={{
        content: `${row.agent.hideIP ? "Enable" : "Disable"} Agent IP visibility for unauthorized users`,
        autoPosition: "true",
        position: "bottom",
      }}
    >
      <Sunglasses />
    </button>
  {/if}
</div>
