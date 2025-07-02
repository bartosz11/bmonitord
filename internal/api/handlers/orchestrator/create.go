package orchestrator

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
	"strings"
)

func HandleCreateOrchestrator(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		orchCreateReq := struct {
			Name string `json:"name" binding:"required"`
			Host string `json:"host" binding:"required"`
		}{}

		if c.ShouldBind(&orchCreateReq) != nil {
			helpers.BadRequest(c)
			return
		}

		if !strings.HasPrefix(orchCreateReq.Host, "ws://") && !strings.HasPrefix(orchCreateReq.Host, "wss://") {
			c.JSON(http.StatusBadRequest, gin.H{
				"error": "host must start with ws:// or wss://",
			})
			return
		}

		if strings.HasSuffix(orchCreateReq.Host, "/") {
			orchCreateReq.Host = strings.TrimSuffix(orchCreateReq.Host, "/")
		}

		var nameCount int64
		err := db.Model(&model.Orchestrator{}).Where("name = ?", orchCreateReq.Name).Count(&nameCount).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		if nameCount != 0 {
			c.JSON(http.StatusConflict, gin.H{
				"error": "name already taken",
			})
			return
		}

		var hostCount int64
		err = db.Model(&model.Orchestrator{}).Where("host = ?", orchCreateReq.Host).Count(&hostCount).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		if hostCount != 0 {
			c.JSON(http.StatusConflict, gin.H{
				"error": "host already taken",
			})
			return
		}

		orchestrator := model.Orchestrator{
			Name: orchCreateReq.Name,
			Host: orchCreateReq.Host,
		}

		err = db.Create(&orchestrator).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		c.JSON(http.StatusOK, gin.H{
			"orchestrator": orchestrator,
		})
	}
}
