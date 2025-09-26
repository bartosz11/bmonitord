import { writable } from 'svelte/store';
import type { Component } from 'svelte';
import Ping from '$lib/components/dashboard/targets/createParts/Ping.svelte';
import HTTP from '$lib/components/dashboard/targets/createParts/HTTP.svelte';

// Lots of stuff in this file is analogical to ../notifications/utils.ts

// I might change this later to a "generated" solution like in notifications, but right now it can be done like this
export const targetTypeNames = {
	0: "Ping",
	1: "HTTP",
} as Record<number, string>;

export const targetTypeOptions = Object.entries(targetTypeNames).map(([value, name]) => {
	return {label: name, value}
})

export const targetCreateSubFormValidity = writable<boolean>(false);
export const targetCreateSubFormOutput = writable<object>();

export const httpCodes: string[] = Array.from(
	{ length: 500 },
	(_, i) => (i + 100).toString()
);

export const targetCreateSubForms: Record<number, Component> = {
	0: Ping,
	1: HTTP
}