import type { PageLoad } from './$types';
import { alarmApi, notificationApi } from '$lib/api';

export const load: PageLoad = async ( { params }) => {
	const targetId= Number(params.id);
	return {
		alarms: (await alarmApi.targetTargetIDAlarmGet(targetId)).data.data!,
		notifications: (await notificationApi.notificationGet()).data.data!
	};
};
