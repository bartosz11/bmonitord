import { ModelNotificationType } from '$lib/api-client-axios';
import type { Component } from 'svelte';
import { type Writable, writable } from 'svelte/store';
import Discord from '$lib/components/dashboard/notifications/types/Discord.svelte';
import Slack from '$lib/components/dashboard/notifications/types/Slack.svelte';
import Gotify from '$lib/components/dashboard/notifications/types/Gotify.svelte';
import Pushbullet from '$lib/components/dashboard/notifications/types/Pushbullet.svelte';
import Email from '$lib/components/dashboard/notifications/types/Email.svelte';
import GenericWebhook from '$lib/components/dashboard/notifications/types/GenericWebhook.svelte';

export const notificationTypeNames = Object.fromEntries(
	Object.entries(ModelNotificationType).map(([k, v]) => {
		const label = k === 'GenericWebhook' ? 'Generic webhook' : k;
		return [v, label];
	})
) as Record<number, keyof typeof ModelNotificationType>;

export const notificationTypeOptions = Object.entries(notificationTypeNames).map(([value, name]) => {
	return {
		label: name === 'GenericWebhook' ? 'Generic webhook' : name,
		value
	};
});

export const notificationTypeFormParts: Record<number, Component> = {
	0: Discord,
	1: Slack,
	2: Pushbullet,
	3: Email,
	4: Gotify,
	5: GenericWebhook
};

export const subFormState = writable<Writable<unknown>>();
export const subFormOutput = writable<string>();