import type { PageLoad } from './$types';
import { settingsApi } from '$lib/api';

export const load: PageLoad = async () => {
	return {
		settings: (await settingsApi.adminSettingsGet()).data.data!
	};
};