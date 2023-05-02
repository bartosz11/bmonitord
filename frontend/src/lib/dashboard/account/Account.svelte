<script>
  import ChangePasswordForm from "./ChangePasswordForm.svelte";
  import ChangeUsernameForm from "./ChangeUsernameForm.svelte";
  import DeleteAccountCard from "./DeleteAccountCard.svelte";
  import InvalidateSessionsCard from "./InvalidateSessionsCard.svelte";
  import UserInfoCard from "./UserInfoCard.svelte";
</script>

<div class="grid grid-rows-[1fr_auto_auto] md:grid-cols-4 mx-auto container my-4 px-4 gap-2">
  <UserInfoCard />
  <ChangePasswordForm />
  <ChangeUsernameForm />
  <div class="md:col-span-2 flex flex-col md:flex-row gap-2">
    <InvalidateSessionsCard />
    <DeleteAccountCard />
  </div>
</div>
