import type { PageLoad } from './$types';
import { checkerApi } from '$lib/api';

export const load: PageLoad = async () => {
	return {
		checkers: (await checkerApi.adminCheckerGet()).data.data!
	};
};
