<script>
  import http from "@/http";
  import { deleteCookie } from "svelte-cookie";
  import { closeAllModals, openModal } from "svelte-modals";
  import { push } from "svelte-spa-router";
  import DeleteAccountConfirmationModal from "./DeleteAccountConfirmationModal.svelte";
  import toast from "svelte-french-toast";

  function onClick() {
    openModal(DeleteAccountConfirmationModal, {
      onConfirm: () => {
        toast.promise((async () => {
          const response = await http.delete("/api/user")
          if (response.status === 204) {
            deleteCookie("auth-token");
            push("/auth/login");
            return;
          }
        })(), {
          error: "Something went wrong while deleting your account.",
          success: "Your account has been successfully deleted.",
          loading: "Deleting your account..."
        }, { style: 'border-radius: 200px; background: #333; color: #fff;' })
        .finally(() => closeAllModals());
      }
    })
  }
</script>

<div class="card">
  <span class="font-bold text-xl">Delete account</span>
  <div>
    <button class="btn-danger-primary mt-3" on:click={onClick}>
      Delete account
    </button>
  </div>
</div>
