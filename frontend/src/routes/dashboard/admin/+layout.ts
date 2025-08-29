import type { LayoutLoad } from './$types';
import { goto } from '$app/navigation';

export const load: LayoutLoad = async ({ parent }) => {
	const user = (await parent()).user;
	if (user === undefined || !user.admin) await goto("/dashboard/targets");
	return {};
};