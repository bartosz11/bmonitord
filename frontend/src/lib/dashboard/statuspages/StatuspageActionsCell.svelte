<script>
  import http from "@/utils/httpUtil";
  import { openModal } from "svelte-modals";
  import StatuspageAnnouncementModal from "./StatuspageAnnouncementModal.svelte";
  import StatuspageEditModal from "./StatuspageEditModal.svelte";
  import StatuspageMonitorsModal from "./StatuspageMonitorsModal.svelte";
  import { tooltip } from "@svelte-plugins/tooltips";
  import {
    Trash,
    Pencil,
    Megaphone,
    Pulse,
    ArrowUpRight,
    Globe,
  } from "phosphor-svelte";
  import { error, promise } from "@/utils/toastUtil";
  import StatuspageAssignDomainModal from "./StatuspageAssignDomainModal.svelte";
  import ConfirmationModal from "@/lib/ConfirmationModal.svelte";
  export let row;
  let count = 0;

  function onDeleteClick() {
    openModal(ConfirmationModal, {
      title: `Are you sure you want to delete ${row.name}?`,
      onConfirm: () => {
        promise(http.delete(`/api/statuspage/${row.id}`), {
          success: `Successfully deleted ${row.name}!`,
          error: null,
          loading: "Deleting statuspage...",
        })
          .then(() =>  location.reload())
          .catch((err) =>
          error(
            err.response?.data?.errors[0]?.message ??
              "Something went wrong while deleting statuspage."
          )
        )
      }
    })
  }

  function onEditClick() {
    openModal(StatuspageEditModal, { statuspage: row });
  }

  function onAnnouncementClick() {
    openModal(StatuspageAnnouncementModal, {
      announcement: row.announcement,
      statuspageId: row.id,
    });
  }

  function onMonitorsClick() {
    openModal(StatuspageMonitorsModal, { statuspage: row });
  }

  function onDomainsClick() {
    openModal(StatuspageAssignDomainModal, { row: row });
  }

  function onRedirectClick() {
    window.open(`#/statuspage/${row.id}`, "_blank").focus();
  }
</script>

<div class="flex flex-row space-x-2 m-2">
  <!--delete-->
  <button
    on:click={onDeleteClick}
    class="border border-red-500 p-1 w-fit h-fit text-red-500 hover:bg-red-500 hover:text-white text-xl"
    use:tooltip={{
      content: "Delete statuspage",
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
      content: "Edit statuspage",
      autoPosition: "true",
      position: "bottom",
    }}
  >
    <Pencil />
  </button>
  <!--announcement-->
  <button
    on:click={onAnnouncementClick}
    class="border border-blue-500 p-1 w-fit h-fit text-blue-500 hover:bg-blue-500 hover:text-white text-xl"
    use:tooltip={{
      content: "Manage statuspage announcement",
      autoPosition: "true",
      position: "bottom",
    }}
  >
    <Megaphone />
  </button>
  <!--domains-->
  <button
    on:click={onDomainsClick}
    class="border border-blue-500 p-1 w-fit h-fit text-blue-500 hover:bg-blue-500 hover:text-white text-xl"
    use:tooltip={{
      content: "Manage statuspage's domain",
      autoPosition: "true",
      position: "bottom",
    }}
  >
    <Globe />
  </button>
  <!--monitors-->
  <button
    on:click={onMonitorsClick}
    class="border border-blue-500 p-1 w-fit h-fit text-blue-500 hover:bg-blue-500 hover:text-white text-xl"
    use:tooltip={{
      content: "View monitors added to this statuspage",
      autoPosition: "true",
      position: "bottom",
    }}
  >
    <Pulse />
  </button>
  <!--redirect-->
  <button
    on:click={onRedirectClick}
    class="border border-blue-500 p-1 w-fit h-fit text-blue-500 hover:bg-blue-500 hover:text-white text-xl"
    use:tooltip={{
      content: "Open statuspage in another tab",
      autoPosition: "true",
      position: "bottom",
    }}
  >
    <ArrowUpRight />
  </button>
</div>
