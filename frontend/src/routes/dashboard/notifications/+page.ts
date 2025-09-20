import type { PageLoad } from './$types';
import { notificationApi } from '$lib/api';

export const load: PageLoad = async () => {
	return {
		notifications: (await notificationApi.notificationGet()).data.data!
	};
};