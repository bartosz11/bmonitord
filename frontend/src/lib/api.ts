import { AlarmApi, AuthApi, CheckerApi, Configuration,
	HeartbeatApi, NotificationApi, OrchestratorApi, SessionApi, SettingsApi, TargetApi, UserApi } from "./api-client-axios";

const config = new Configuration({
	basePath: import.meta.env.VITE_API_BASE_URL ?? '/api',
	baseOptions: { 
		withCredentials: true,
	}
});

export const alarmApi = new AlarmApi(config);
export const authApi = new AuthApi(config);
export const checkerApi = new CheckerApi(config);
export const notificationApi = new NotificationApi(config);
export const orchestratorApi = new OrchestratorApi(config);
export const sessionApi = new SessionApi(config);
export const settingsApi = new SettingsApi(config);
export const targetApi = new TargetApi(config);
export const userApi = new UserApi(config);
export const heartbeatApi = new HeartbeatApi(config);