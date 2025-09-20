import type { Validator } from 'svelte-use-form';

export function prefixes(prefixes: string[]): Validator {
	return (value) => {
		let startsWithAny = false;
		for (const prefix of prefixes) {
			if (value.startsWith(prefix)) {
				startsWithAny = true;
				break;
			}
		}
		return startsWithAny ? null : { prefixes: "value doesn't start with prefix" }
	}
}