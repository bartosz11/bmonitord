import type { PageLoad } from './$types';
import { targetApi } from '$lib/api';

export const load: PageLoad = async () => {
	return {
		targets: (await targetApi.targetGet()).data.data!
	};
};
