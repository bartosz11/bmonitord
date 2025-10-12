package tasks

import (
	"github.com/bartosz11/checkmate/orchestrator/helpers"
	"github.com/coder/websocket"
	"github.com/rs/zerolog/log"
)

func BroadcastPingTask() func() {
	return func() {
		pingMsg := helpers.WebSocketMessage{
			Type: "ping",
		}

		WSConnections.Range(func(k any, v any) bool {
			conn := v.(*websocket.Conn)
			log.Trace().Any("checker", k).Msg("sending ping msg")
			helpers.SendJSON(conn, pingMsg)
			return true
		})
	}
}
