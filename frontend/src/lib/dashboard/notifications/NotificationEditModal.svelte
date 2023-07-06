<script>
  import http from "@/utils/httpUtil";
  import { error, success } from "@/utils/toastUtil";
  import { closeModal } from "svelte-modals";
  import { Hint, required, useForm, validators } from "svelte-use-form";

  export let isOpen;
  export let notification;
  let name = notification.name;
  let type = notification.type;
  let fieldsSplit = notification.credentials.split("|");
  let field1 = fieldsSplit[0];
  let placeholder1;
  let placeholder2;
  let field2 = fieldsSplit[1] ?? undefined;
  $: {
    switch (type) {
      case "EMAIL":
        placeholder1 = "Email address";
        break;
      case "DISCORD":
        placeholder1 = "Discord webhook URL";
        break;
      case "SLACK":
        placeholder1 = "Slack webhook URL";
        break;
      case "GENERIC_WEBHOOK":
        placeholder1 = "Webhook URL";
        break;
      case "PUSHBULLET":
        placeholder1 = "Pushbullet API key";
        break;
      case "GOTIFY":
        placeholder1 = "Gotify URL";
        placeholder2 = "Gotify app key";
        break;
    }
  }
  function onSubmit(e) {
    e.preventDefault();
    http
      .patch(`/api/notification/${notification.id}`, {
        name: name,
        type: type,
        credentials: [field1, field2].filter(Boolean).join("|"),
      })
      .then((response) => {
        success("Successfully edited notification.");
        location.reload();
      })
      .catch((err) => {
        error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while editing notification."
        );
      });
  }
  const form = useForm();
</script>

{#if isOpen}
  <div role="dialog" class="modal">
    <div class="modal-contents">
      <form use:form on:submit={onSubmit}>
        <h1>Edit notification</h1>
        <div>
          <input
            class="input-primary mt-4"
            type="text"
            bind:value={name}
            placeholder="Name"
            use:validators={[required]}
            name="name"
          />
          <Hint for="name" on="required" class="hint-primary"
            >This field is required.</Hint
          >
        </div>
        <div class="mt-4">
          <label for="type">Type</label>
          <select
            name="type"
            class="input-primary"
            placeholder="Select type"
            bind:value={type}
            use:validators={[required]}
          >
            <option value="EMAIL">Email</option>
            <option value="DISCORD">Discord</option>
            <option value="SLACK">Slack</option>
            <option value="GENERIC_WEBHOOK">Generic webhook</option>
            <option value="PUSHBULLET">Pushbullet</option>
            <option value="GOTIFY">Gotify</option>
          </select>
          <Hint for="type" on="required" class="hint-primary"
            >This field is required.</Hint
          >
        </div>
        <div>
          <input
            class="input-primary mt-4"
            type="text"
            bind:value={field1}
            placeholder={placeholder1}
            use:validators={[required]}
            name="field1"
          />
          <Hint for="field1" on="required" class="hint-primary"
            >This field is required.</Hint
          >
        </div>
        {#if type === "GOTIFY"}
          <div>
            <input
              class="input-primary mt-4"
              type="text"
              bind:value={field2}
              placeholder={placeholder2}
              use:validators={[required]}
              name="field2"
            />
            <Hint for="field2" on="required" class="hint-primary"
              >This field is required.</Hint
            >
          </div>
        {/if}
        <div class="mt-4">
          <button
            class="btn-danger-primary"
            type="button"
            on:click={() => closeModal()}>Cancel</button
          >
          <button class="btn-ok-primary" disabled={!$form.valid}>Edit</button>
        </div>
      </form>
    </div>
  </div>
{/if}
