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
			resp := helpers.HTTPResponse{
				Code:  http.StatusBadRequest,
				Error: "host must start with ws:// or wss://",
			}
			resp.WriteAsJSON(c)
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
			resp := helpers.HTTPResponse{
				Code:  http.StatusConflict,
				Error: "name already taken",
			}
			resp.WriteAsJSON(c)
			return
		}

		var hostCount int64
		err = db.Model(&model.Orchestrator{}).Where("host = ?", orchCreateReq.Host).Count(&hostCount).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		if hostCount != 0 {
			resp := helpers.HTTPResponse{
				Code:  http.StatusConflict,
				Error: "host already taken",
			}
			resp.WriteAsJSON(c)
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

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: orchestrator,
		}
		resp.WriteAsJSON(c)
	}
}
