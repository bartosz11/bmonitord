import type { PageLoad } from './$types';
import { sessionApi } from '$lib/api';

export const load: PageLoad = async () => {
	return {
		sessions: (await sessionApi.sessionGet()).data.data!
	};
};
