import { toDatetimeLocal } from '$lib/utils';

export type SettingInfo = {
	readonly: boolean,
	inputType: 'date' | 'checkbox' | 'text' | 'number' | 'datetime-local',
	displayKey: string,
	// HTML accepts stringified values I guess
	valueConverter: (value: string | undefined) => string
};

export const settingsInfo = new Map<string, SettingInfo>([
	[
		'registration-enabled',
		{
			readonly: false,
			inputType: 'checkbox',
			displayKey: "Registration enabled",
			valueConverter: (value) => {
				// sign-in is enabled by default and if the setting value is exactly "true", else disabled
				if (value === undefined || value === "true") return "true";
				else return "false";
			}
		}
	],
	[
		'last-broadcast-task-started',
		{
			readonly: true,
			inputType: 'datetime-local',
			displayKey: "Last broadcast task started (adjusted to your timezone)",
			valueConverter: (value) => {
				if (value === undefined) return "";
				const date = new Date(parseInt(value));
				return toDatetimeLocal(date);
			}
		}
	]
]);
