<script>
  import http from "@/utils/httpUtil";
  import { error, promise, success } from "@/utils/toastUtil";
  import { tooltip } from "@svelte-plugins/tooltips";
  import { Pulse, BellRinging, Pencil, Trash } from "phosphor-svelte";
  import { openModal } from "svelte-modals";
  import NotificationEditModal from "./NotificationEditModal.svelte";
  import NotificationMonitorsModal from "./NotificationMonitorsModal.svelte";
  import ConfirmationModal from "@/lib/ConfirmationModal.svelte";
  export let row;

  function onDeleteClick() {
    openModal(ConfirmationModal, {
      title: `Are you sure you want to delete ${row.name}?`,
      onConfirm: () => {
        promise(http.delete(`/api/notification/${row.id}`), {
          success: `Successfully deleted ${row.name}!`,
          error: null,
          loading: "Deleting notification...",
        })
          .then(() =>  location.reload())
          .catch((err) =>
          error(
            err.response?.data?.errors[0]?.message ??
              "Something went wrong while deleting notification."
          )
        )
      }
    })
  }

  function onEditClick() {
    openModal(NotificationEditModal, { notification: row });
  }

  function onTestClick() {
    http
      .post(`/api/notification/${row.id}/test`)
      .then((response) => {
        success("Successfully sent a test notification.");
      })
      .catch((err) =>
        error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while sending test notification."
        )
      );
  }
  function onMonitorsClick() {
    openModal(NotificationMonitorsModal, { notification: row });
  }
</script>

<div class="flex flex-row space-x-2 m-2">
  <!--delete-->
  <button
    on:click={onDeleteClick}
    class="border border-red-500 p-1 w-fit h-fit text-red-500 hover:bg-red-500 hover:text-white text-xl"
    use:tooltip={{
      content: "Delete notification",
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
      content: "Edit notification",
      autoPosition: "true",
      position: "bottom",
    }}
  >
    <Pencil />
  </button>
  <!--test-->
  <button
    on:click={onTestClick}
    class="border border-blue-500 p-1 w-fit h-fit text-blue-500 hover:bg-blue-500 hover:text-white text-xl"
    use:tooltip={{
      content: "Send test notification",
      autoPosition: "true",
      position: "bottom",
    }}
  >
    <BellRinging />
  </button>
  <!--monitors-->
  <button
    on:click={onMonitorsClick}
    class="border border-blue-500 p-1 w-fit h-fit text-blue-500 hover:bg-blue-500 hover:text-white text-xl"
    use:tooltip={{
      content: "View monitors using this notification",
      autoPosition: "true",
      position: "bottom",
    }}
  >
    <Pulse />
  </button>
</div>
