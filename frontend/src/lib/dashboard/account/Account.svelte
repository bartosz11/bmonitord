<script>
  import ChangePasswordForm from "./ChangePasswordForm.svelte";
  import ChangeUsernameForm from "./ChangeUsernameForm.svelte";
  import DeleteAccountCard from "./DeleteAccountCard.svelte";
  import InvalidateSessionsCard from "./InvalidateSessionsCard.svelte";
    
</script>


<div class="flex flex-col md:flex-row md:space-x-8 mt-8 ml-8">
    <ChangePasswordForm/>
    <ChangeUsernameForm/>
    <InvalidateSessionsCard/>
    <DeleteAccountCard/>
</div> 