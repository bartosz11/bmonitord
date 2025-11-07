package handlers

import (
	"encoding/json"
	"errors"
	"strconv"

	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/bartosz11/checkmate/orchestrator/helpers"
	"github.com/bartosz11/checkmate/orchestrator/tasks"
	"github.com/bartosz11/checkmate/orchestrator/tasks/processing"
	"github.com/coder/websocket"
	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog/log"
	"gorm.io/gorm"
)

func HandleNewWSConnection(db *gorm.DB) func(ctx *gin.Context) {
	return func(ctx *gin.Context) {
		ip := ctx.ClientIP()
		conn, err := websocket.Accept(ctx.Writer, ctx.Request, &websocket.AcceptOptions{})
		if err != nil {
			log.Err(err).Msg(ip + ": websocket upgrade error")
			return
		}
		//this makes a "return" inside this function close the connection, very epic ergonomics
		defer conn.Close(websocket.StatusNormalClosure, "")

		for {
			//Read a single message, we don't need the type
			_, bytes, err := conn.Read(ctx)
			if err != nil {
				//Most likely means the connection is gone, so remove it
				tasks.WSConnections.Range(func(k any, v any) bool {
					if conn == v {
						tasks.WSConnections.Delete(k)
						return false
					}
					return true
				})
				log.Err(err).Msg("websocket read error")
				break // If we break out of this for loop, deferred Close is called so the connection is also closed
			}

			wsMsg := helpers.WebSocketMessage{}
			err = json.Unmarshal(bytes, &wsMsg)
			if err != nil {
				log.Err(err).Msg("websocket message unmarshal error")
				continue
			}

			switch wsMsg.Type {
			case "auth":
				var token string
				err := json.Unmarshal(wsMsg.Payload, &token)
				if err != nil {
					helpers.SendJSON(conn, helpers.WebSocketMessage{Type: "error", Payload: json.RawMessage(`"failed to parse key"`)})
					log.Err(err).Msg("websocket auth key unmarshal error")
					break
				}

				var checker model.Checker
				result := db.First(&checker, "key = ?", token)
				if errors.Is(result.Error, gorm.ErrRecordNotFound) {
					helpers.SendJSON(conn, helpers.WebSocketMessage{Type: "end", Payload: json.RawMessage(`"invalid auth key"`)})
					log.Info().Msg(ip + ": connection denied - invalid auth key")
					return
				}

				checkerId := checker.ID
				tasks.WSConnections.Store(checker.ID, conn)
				idString := strconv.Itoa(int(checkerId))
				helpers.SendJSON(conn, helpers.WebSocketMessage{Type: "info", Payload: json.RawMessage(`"auth-successful ` + idString + `"`)})
				log.Info().Msg(ip + ": connected - auth successful - checker id: " + idString)
				break
			case "result":
				if !isAuthorized(conn) {
					helpers.SendJSON(conn, helpers.WebSocketMessage{Type: "error", Payload: json.RawMessage(`"unauthorized"`)})
					break
				}

				var hb model.Heartbeat
				err := json.Unmarshal(wsMsg.Payload, &hb)
				if err != nil {
					helpers.SendJSON(conn, helpers.WebSocketMessage{Type: "error", Payload: json.RawMessage(`"failed to parse result"`)})
					log.Err(err).Msg("websocket result unmarshal error")
					break
				}

				load, _ := processing.Tasks.Load(hb.TargetID)
				// Theoretically there's the "ok" returned but checking for nil does the same thing
				if load == nil {
					helpers.SendJSON(conn, helpers.WebSocketMessage{Type: "error", Payload: json.RawMessage(`"target not queued"`)})
					break
				}
				task := load.(*processing.Task)
				task.Heartbeats <- hb
				task.CompleteCheckers = append(task.CompleteCheckers, *hb.CheckerID)
				helpers.SendJSON(conn, helpers.WebSocketMessage{Type: "info", Payload: json.RawMessage(`"check-ok"`)})
				break
			case "pong":
				// Do nothing, I guess?
				break
			default:
				message := helpers.WebSocketMessage{Type: "error", Payload: json.RawMessage(`"invalid message type"`)}
				helpers.SendJSON(conn, message)
				break
			}
		}
	}
}

func isAuthorized(conn *websocket.Conn) bool {
	authorized := false
	tasks.WSConnections.Range(func(_ any, v any) bool {
		if v == conn {
			authorized = true
			return false
		}
		return true
	})
	return authorized
}
