import Unavailable from '$lib/components/dashboard/alarms/subforms/Unavailable.svelte';
import Threshold from '$lib/components/dashboard/alarms/subforms/Threshold.svelte';
import type { Component } from 'svelte';
import { writable } from 'svelte/store';


export const alarmTypeNames = {
	// TODO: maybe change these names to something more understandable
	0: 'Unavailable',
	1: 'Threshold'
} as Record<number, string>;

export const alarmTypeOptions = Object.entries(alarmTypeNames).map(([value, name]) => {
	return { label: name, value };
});

export const alarmSubForms = {
	0: Unavailable,
	1: Threshold
} as Record<number, Component>

export const alarmSubFormValidity = writable(false);
export const alarmSubFormOutput = writable<object>({});