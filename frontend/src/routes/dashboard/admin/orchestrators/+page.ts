import type { PageLoad } from './$types';
import { orchestratorApi } from '$lib/api';

export const load: PageLoad = async () => {
	return {
		orchestrators: (await orchestratorApi.adminOrchestratorGet()).data.data!
	};
};