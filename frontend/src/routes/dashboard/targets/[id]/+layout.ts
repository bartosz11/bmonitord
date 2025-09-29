import type { LayoutLoad } from './$types';
import { targetApi } from '$lib/api';

export const load: LayoutLoad = async ({ params }) => {
	const id = params.id;
	return {
		target: (await targetApi.targetTargetIDGet(Number(id))).data.data!
	};
};
