package tasks

import (
	"github.com/bartosz11/checkmate/internal/orchestrator/helpers"
	"github.com/rs/zerolog/log"
)

func BroadcastPingTask() func() {
	return func() {
		pingMsg := helpers.WebSocketMessage{
			Type: "ping",
		}

		for u, conn := range WSConnections {
			log.Trace().Uint("checker", u).Msg("sending ping msg")
			helpers.SendJSON(conn, pingMsg)
		}
	}
}
