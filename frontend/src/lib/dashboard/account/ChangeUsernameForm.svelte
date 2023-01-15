<script>
    import axios from "axios";
    import { useForm, validators, required, Hint } from "svelte-use-form";
    import toast from "svelte-french-toast";
    import { push } from "svelte-spa-router";
    import { getCookie } from "svelte-cookie";
  
    const form = useForm();
    let username;
  
    function onSubmit(e) {
      e.preventDefault();
      axios
        .patch(
          `/api/user/username?username=${username}`,
          {},
          {
            headers: {
              Authorization: `Bearer ${getCookie("auth-token")}`,
            },
          }
        )
        .then((response) => {
          toast.success("Changed username successfully.");
          push("/auth/logout");
        }).catch(err => { 
          if (err.response && err.response.data.errors) {
            toast.error(err.response.data.errors[0].message);
          } else toast.error("Something went wrong while changing your username.");
        });
    }
  </script>
  
  <div class="card">
    <span>Change username</span>
    <form use:form on:submit={onSubmit}>
      <div class="my-3">
        <input
          type="username"
          bind:value={username}
          name="newusername"
          placeholder="New username"
          use:validators={[required]}
          class="input-primary"
        />
        <Hint class="hint-primary" for="newusername" on="required"
          >This field is required.</Hint
        >
      </div>
      
      <button disabled={!$form.valid} class="btn-ok-primary"
        >Change username</button
      >
    </form>
  </div>
  