import type { LayoutLoad } from './$types';
import { userApi } from '$lib/api';
import { goto } from '$app/navigation';
import Cookies from 'js-cookie';

export const ssr = false;

export const load = (async ({ url }) => {
	const pathname = url.pathname.toLowerCase();
	try {
		const resp = await userApi.userGet();
		// success, session is valid
		if (pathname.startsWith('/auth/') || pathname === "/") {
			await goto('/dashboard/targets');
		}
		return { user: resp.data.data! }
	} catch {
		// session is invalid for whatever reason, if user is not browsing a public page, throw them at the login page
		if ((!pathname.startsWith('/auth/') && !pathname.startsWith("/report/")) || pathname === '/') {
			Cookies.remove('auth-token');
			await goto('/auth/login');
		}
	}

	return {};
}) satisfies LayoutLoad;
