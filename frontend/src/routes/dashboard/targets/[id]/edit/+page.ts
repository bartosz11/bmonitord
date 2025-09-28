import type { PageLoad } from './$types';
import { targetApi, checkerApi } from '$lib/api';

export const load: PageLoad = async ({params }) => {
	const id = params.id;
	return {
		target: (await targetApi.targetTargetIDGet(Number(id))).data.data!,
		checkers: (await checkerApi.checkerGet()).data.data!,
	};
};
