<script>
  import http from "@/utils/httpUtil";
  import { error, promise } from "@/utils/toastUtil";
  import { tooltip } from "@svelte-plugins/tooltips";
  import {
    ListNumbers,
    Pencil,
    Trash
  } from "phosphor-svelte";
  import { openModal } from "svelte-modals";
  import DomainEditModal from "./DomainEditModal.svelte";
  import DomainSetupModal from "./DomainSetupModal.svelte";
  import ConfirmationModal from "@/lib/ConfirmationModal.svelte";
  export let row;

  function onDeleteClick() {
    openModal(ConfirmationModal, {
      title: `Are you sure you want to delete ${row.name}?`,
      onConfirm: () => {
        promise(http.delete(`/api/domain/${row.id}`), {
          success: `Successfully deleted ${row.name}!`,
          error: null,
          loading: "Deleting domain...",
        })
          .then(() =>  location.reload())
          .catch((err) =>
          error(
            err.response?.data?.errors[0]?.message ??
              "Something went wrong while deleting domain."
          )
        )
      }
    })
  }

  function onEditClick() {
    openModal(DomainEditModal, { domain: row });
  }
  
  function onSetupClick() {
    openModal(DomainSetupModal, { domain: row });
  }

</script>

<div class="flex flex-row space-x-2 m-2">
  <!--delete-->
  <button
    on:click={onDeleteClick}
    class="border border-red-500 p-1 w-fit h-fit text-red-500 hover:bg-red-500 hover:text-white text-xl"
    use:tooltip={{
      content: "Delete domain",
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
      content: "Edit domain",
      autoPosition: "true",
      position: "bottom",
    }}
  >
    <Pencil />
  </button>
  <!--setup-->
  <button
    on:click={onSetupClick}
    class="border border-blue-500 p-1 w-fit h-fit text-blue-500 hover:bg-blue-500 hover:text-white text-xl"
    use:tooltip={{
      content: "View setup instructions",
      autoPosition: "true",
      position: "bottom",
    }}
  >
    <ListNumbers />
  </button>
</div>
