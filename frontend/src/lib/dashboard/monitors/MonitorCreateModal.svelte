<script>
  import http from "@/http";
  import { closeModal } from "svelte-modals";
  import {
    Hint,
    HintGroup,
    number,
    required,
    useForm,
    validators,
  } from "svelte-use-form";
  import { error, success } from "@/toastUtil";

  export let isOpen;
  let name;
  let type = "HTTP";
  let host;
  let timeout;
  let retries;
  let allowedHttpCodes;
  let published;
  let verifyCertificate = false;
  let followRedirects = true;
  const form = useForm();
  function onSubmit(e) {
    e.preventDefault();
    http
      .post(`/api/monitor`, {
        name: name,
        type: type,
        host: host ?? "",
        timeout: timeout,
        retries: retries,
        published: published,
        httpInfo: {
          allowedHttpCodes: allowedHttpCodes ?? "",
          verifyCertificate: verifyCertificate ?? false,
          followRedirects: followRedirects ?? true,
        }
      })
      .then((response) => {
        success("Successfully created a new monitor.");
        location.reload();
      })
      .catch((err) => {
        error(
          err.response?.data?.errors[0]?.message ??
            "Something went wrong while creating monitor."
        );
      });
  }
</script>

{#if isOpen}
  <div role="dialog" class="modal">
    <div class="modal-contents w-80">
      <form use:form on:submit={onSubmit} class="space-y-4">
        <h1>Create monitor</h1>
        <div>
          <input
            class="input-primary w-full"
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
        <div>
          <label for="type">Type</label>
          <select
            name="type"
            class="input-primary"
            placeholder="Select type"
            bind:value={type}
            use:validators={[required]}
          >
            <option value="HTTP">HTTP/HTTPS</option>
            <option value="PING">Ping</option>
            <option value="AGENT">Server Agent</option>
          </select>
          <Hint for="type" on="required" class="hint-primary"
            >This field is required.</Hint
          >
        </div>
        {#if type !== "AGENT"}
          <div>
            <input
              class="input-primary w-full"
              type="text"
              bind:value={host}
              placeholder="Host"
              use:validators={[required]}
              name="host"
            />
            <Hint for="host" on="required" class="hint-primary"
              >This field is required.</Hint
            >
          </div>
          <div>
            <input
              class="input-primary w-full"
              type="text"
              bind:value={retries}
              placeholder="Retries"
              use:validators={[required, number]}
              name="retries"
            />
            <HintGroup>
              <Hint for="retries" on="required" class="hint-primary"
                >This field is required.</Hint
              >
              <Hint
                hideWhenRequired
                for="retries"
                on="number"
                class="hint-primary">A number is expected.</Hint
              >
            </HintGroup>
          </div>
        {/if}
        <div>
          <input
            class="input-primary w-full"
            type="text"
            bind:value={timeout}
            placeholder="Timeout (seconds)"
            use:validators={[required, number]}
            name="timeout"
          />
          <HintGroup>
            <Hint for="timeout" on="required" class="hint-primary"
              >This field is required.</Hint
            >
            <Hint
              hideWhenRequired
              for="timeout"
              on="number"
              class="hint-primary">A number is expected.</Hint
            >
          </HintGroup>
        </div>
        {#if type === "HTTP"}
          <div>
            <input
              class="input-primary w-full"
              type="text"
              bind:value={allowedHttpCodes}
              placeholder="Allowed HTTP codes (separate with ,)"
              use:validators={[required]}
              name="allowedHttpCodes"
            />
            <Hint for="allowedHttpCodes" on="required" class="hint-primary"
              >This field is required.</Hint
            >
          </div>
          <div class="space-x-2">
            <label for="verifyCertificate">Verify SSL certificate</label>
            <input
              bind:checked={verifyCertificate}
              class="input-primary"
              type="checkbox"
              name="verifyCertificate"
            />
          </div>
          <div class="space-x-2">
            <label for="followRedirects">Follow redirects</label>
            <input
              bind:checked={followRedirects}
              class="input-primary"
              type="checkbox"
              name="followRedirects"
            />
          </div>
        {/if}
        <div class="space-x-2">
          <label for="published">Public</label>
          <input
            bind:checked={published}
            class="input-primary"
            type="checkbox"
            name="published"
          />
        </div>
        <div class="flex flex-row space-x-2">
          <button
            class="btn-danger-primary w-1/3"
            type="button"
            on:click={() => closeModal()}>Cancel</button
          >
          <button class="btn-ok-primary w-2/3" disabled={!$form.valid}
            >Create</button
          >
        </div>
      </form>
    </div>
  </div>
{/if}
