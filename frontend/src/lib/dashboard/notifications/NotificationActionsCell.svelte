<script>
  import http from "@/http";
  import toast from "svelte-french-toast";
  import { openModal } from "svelte-modals";
  import NotificationEditModal from "./NotificationEditModal.svelte";
  import NotificationMonitorsModal from "./NotificationMonitorsModal.svelte";
  export let row;
  let count = 0;

  function onDeleteClick() {
    count++;
    if (count === 1) toast("Are you sure?", { duration: 2000 });
    if (count >= 2)
      http
        .delete(`/api/notification/${row.id}`)
        .then((response) => {
          location.reload();
          toast.success(`Successfully deleted notification with ID ${row.id}`);
        })
        .catch((err) =>
          toast.error(
            err.response?.data?.errors[0]?.message ??
              "Something went wrong while deleting notification."
          )
        );
  }

  function onEditClick() {
    openModal(NotificationEditModal, { notification: row });
  }

  function onTestClick() {
    http
      .post(`/api/notification/${row.id}/test`)
      .then((response) => {
        toast.success("Successfully sent a test notification.");
      })
      .catch((err) =>
        toast.error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while sending test notification."
        )
      );
  }
  function onMonitorsClick() { 
    openModal(NotificationMonitorsModal, {notification: row});
  }
</script>

<div class="flex flex-row space-x-2 m-2">
  <!--delete-->
  <button
    on:click={onDeleteClick}
    class="border border-red-500 p-1 w-fit h-fit text-red-500 hover:bg-red-500 hover:text-white"
  >
    <i class="ph-trash" />
  </button>
  <!--edit-->
  <button
    on:click={onEditClick}
    class="border border-blue-500 p-1 w-fit h-fit text-blue-500 hover:bg-blue-500 hover:text-white"
  >
    <i class="ph-pencil" />
  </button>
  <!--test-->
  <button
    on:click={onTestClick}
    class="border border-blue-500 p-1 w-fit h-fit text-blue-500 hover:bg-blue-500 hover:text-white"
  >
    <i class="ph-bell-ringing" />
  </button>
  <!--monitors-->
  <button on:click={onMonitorsClick} class="border border-blue-500 p-1 w-fit h-fit text-blue-500 hover:bg-blue-500 hover:text-white">
    <i class="ph-activity"/>
  </button>
</div>
