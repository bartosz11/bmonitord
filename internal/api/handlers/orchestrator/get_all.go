package orchestrator

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

func HandleGetAllOrchestrators(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		var orchestrators []model.Orchestrator
		err := db.Find(&orchestrators).Error

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: orchestrators,
		}
		resp.WriteAsJSON(c)
	}
}
