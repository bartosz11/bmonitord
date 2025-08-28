import { writable } from 'svelte/store';
import type { Snippet } from 'svelte';

export const dashboardHeader = writable("");
export const additionalButtons = writable<Snippet | null>(null);