import type { PageLoad } from './$types';
import { targetApi } from '$lib/api';

export const load = (async ({params}) => {
    return {
			data: (await targetApi.targetTargetIDReportGet(Number(params.id))).data.data!
		};
}) satisfies PageLoad;