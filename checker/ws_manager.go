package checker

import (
	"context"
	"encoding/json"
	"errors"
	"strconv"
	"strings"

	"github.com/bartosz11/checkmate/checker/checkproviders"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/bartosz11/checkmate/orchestrator/helpers"
	"github.com/coder/websocket"
	"github.com/coder/websocket/wsjson"
	"github.com/rs/zerolog/log"
)

func Connect(host string, key string) {
	// Constantly retry connecting to each orchestrator
	for {
		attemptConnecting(host, key)
	}
}

func attemptConnecting(host string, key string) {
	ctx := context.Background() // We don't want timeouts or anything

	conn, _, err := websocket.Dial(ctx, host, nil)
	if err != nil {
		log.Error().Err(err).Str("host", host).Msg("failed to connect to orchestrator")
		return
	}
	defer conn.CloseNow()

	authMsg := helpers.WebSocketMessage{
		Type:    "auth",
		Payload: json.RawMessage(`"` + key + `"`),
	}

	if err = wsjson.Write(ctx, conn, &authMsg); err != nil {
		log.Info().Str("error", err.Error()).Msg("error str")
		log.Error().Err(err).Str("host", host).Msg("failed to write authentication msg to orchestrator connection")
		_ = conn.Close(websocket.StatusProtocolError, "")
		// since this function is ran in an infinite loop, this will retry connecting
		return
	}

	var selfId uint64

	for {
		var msg helpers.WebSocketMessage
		if err = wsjson.Read(ctx, conn, &msg); err != nil {
			// We ignore the JSON errors if they occur only after auth because we got the data needed to operate (selfId)
			if isJSONError(err) && selfId != 0 {
				log.Warn().Str("orchestrator", host).Err(err).Msg("json sent by orchestrator caused an error")
				continue
			}
			// All other errors pretty much mean that the connection is gone, may as well try reconnecting
			return
		}

		if msg.Type == "check" {
			if selfId == 0 {
				log.Warn().Str("orchestrator", host).Msg("check request received while unauthenticated, check won't be ran")
				continue
			}

			var target model.Target
			if err = json.Unmarshal(msg.Payload, &target); err != nil {
				// this is fine-ish, orchestrator will just make an "unknown" status heartbeat
				log.Error().Err(err).Str("orchestrator", host).Msg("failed to unmarshal target sent by orchestrator")
				continue
			}

			heartbeat := checkproviders.CheckProviders[target.Type](target)
			u := uint(selfId)
			heartbeat.CheckerID = &u

			payload, err := json.Marshal(heartbeat)
			if err != nil {
				log.Error().Err(err).Str("orchestrator", host).Msg("failed to marshal check results")
				continue
			}

			resultMsg := helpers.WebSocketMessage{
				Type:    "result",
				Payload: payload,
			}
			helpers.SendJSON(conn, resultMsg)
		} else {
			var payload string
			if err = json.Unmarshal(msg.Payload, &payload); err != nil {
				log.Error().Err(err).Str("orchestrator", host).Msg("failed to unmarshal msg details sent by orchestrator")
				// I think we should restart if not authenticated, payload is quite crucial because we need selfId to run checks
				// otherwise we can ignore as messages of type "check" are the "important" ones now
				// We can afford to continuously reauthenticate, nothing bad is going to happen on the backend
				if selfId == 0 {
					_ = conn.Close(websocket.StatusUnsupportedData, "")
					return
				} else {
					continue
				}
			}
			switch msg.Type {
			case "info":
				payloadSplit := strings.Split(payload, " ")
				if payloadSplit[0] == "auth-successful" {
					checkerId, err := strconv.ParseUint(payloadSplit[1], 10, 64)
					if err != nil {
						log.Error().Err(err).Str("orchestrator", host).Msg("failed to parse checker id from orchestrator")
						// we can close the connection and re-authenticate but this shouldn't happen
						_ = conn.Close(websocket.StatusProtocolError, "")
					}
					log.Info().Str("orchestrator", host).Uint64("checkerId", checkerId).Msg("auth successful to orchestrator")
					selfId = checkerId
				}
				break
			case "end":
				if payload == "invalid auth key" {
					log.Error().Str("orchestrator", host).Msg("failed to authenticate with orchestrator")
					// Restart here because we don't know if the key is invalid or something else happened that could be a one-time thing
					// I don't know what codes to use, couldn't they just reuse the HTTP codes for websockets
					_ = conn.Close(websocket.StatusNormalClosure, "")
					return
				}
				break
			case "error":
				log.Error().Str("error", payload).Str("orchestrator", host).Msg("error message received")
				break
			case "ping":
				pongMsg := helpers.WebSocketMessage{
					Type: "pong",
				}
				helpers.SendJSON(conn, pongMsg)
				break
			}
		}

	}
}

// todo: extract this to a helper fn
func isJSONError(err error) bool {
	if err == nil {
		return false
	}
	var se *json.SyntaxError
	var ute *json.UnmarshalTypeError
	return errors.As(err, &se) || errors.As(err, &ute)
}
