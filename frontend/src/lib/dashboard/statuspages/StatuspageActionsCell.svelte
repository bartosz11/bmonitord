<script>
  import http from "@/http";
  import { openModal } from "svelte-modals";
  import StatuspageAnnouncementModal from "./StatuspageAnnouncementModal.svelte";
  import StatuspageEditModal from "./StatuspageEditModal.svelte";
  import StatuspageMonitorsModal from "./StatuspageMonitorsModal.svelte";
  import { tooltip } from "@svelte-plugins/tooltips";
  import { Trash, Pencil, Megaphone, Activity } from "phosphor-svelte";
  import { error, info, success } from "@/toast-util";
  export let row;
  let count = 0;

  function onDeleteClick() {
    count++;
    if (count === 1) info("Are you sure?", { duration: 2000 });
    if (count >= 2)
      http
        .delete(`/api/statuspage/${row.id}`)
        .then((response) => {
          //i cba changing the data in the table
          location.reload();
          success(`Successfully deleted statuspage with ID ${row.id}`);
        })
        .catch((err) =>
          error(
            err.response?.data?.errors[0]?.message ??
              "Something went wrong while deleting statuspage."
          )
        );
  }

  function onEditClick() {
    openModal(StatuspageEditModal, { id: row.id });
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
      content: "Rename statuspage",
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
    <Activity />
  </button>
</div>
