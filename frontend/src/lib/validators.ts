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
		return startsWithAny ? null : { prefixes: "value doesn't start with prefix" };
	};
}

export const notNegative: Validator = (value) => {
	const num = Number(value);
	if (isNaN(num)) return { notNegative: 'given value is not a number' };
	return num >= 0 ? null : { notNegative: 'given value is negative' };
};
