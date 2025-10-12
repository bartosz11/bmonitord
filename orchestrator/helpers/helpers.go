package helpers

import (
	"context"
	"encoding/json"

	"github.com/coder/websocket"
	"github.com/rs/zerolog/log"
)

//Had to create this due to "cyclic imports" ffs

func SendJSON(conn *websocket.Conn, msg WebSocketMessage) {
	b, err := json.Marshal(msg)
	if err != nil {
		log.Err(err).Bytes("rawMsg", msg.Payload).Msg("ws json marshal error")
		return
	}
	conn.Write(context.Background(), websocket.MessageText, b)
}

type WebSocketMessage struct {
	Type string `json:"type"`
	// We parse payload further to corresponding objects when needed
	Payload json.RawMessage `json:"payload"`
}

func Contains[T comparable](slice []T, val T) bool {
	for _, item := range slice {
		if item == val {
			return true
		}
	}
	return false
}
