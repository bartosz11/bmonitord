package handlers

import (
	"time"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/bartosz11/checkmate/orchestrator/tasks/processing"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

func HandleAgentPost(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		key := c.Param("key")
		var target model.Target
		err := db.Joins("Agent").First(&target, `"Agent"."key" = ?`, key).Error
		if err != nil {
			helpers.NotFound(c)
			return
		}

		var payload model.HeartbeatPayload
		if c.ShouldBind(&payload) != nil {
			helpers.BadRequestWithSpecificError(c, "bad request body")
			return
		}

		payload.Data["ip"] = c.ClientIP()

		hb := model.Heartbeat{
			Timestamp: time.Now(),
			Status:    model.Up,
			TargetID:  target.ID,
			Target:    target,
			Payload:   &payload,
		}

		target.Agent.Installed = true
		target.Agent.LastDataReceived = hb.Timestamp
		// handles all alarm processing and incidents, the same code that orchestrator's check task uses but adjusted for 1-hb, no checker usage
		// this function also saves the heartbeat
		processing.ProcessPushHeartbeat(&target, &hb, db, true)
		db.Save(&(target.Agent))
	}
}
