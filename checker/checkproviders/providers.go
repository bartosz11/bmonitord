package checkproviders

import "github.com/bartosz11/checkmate/common/database/model"

var CheckProviders = map[model.TargetType]func(target model.Target) model.Heartbeat{
	model.HTTP: RunHTTPCheck,
	model.PING: RunPingCheck,
}
