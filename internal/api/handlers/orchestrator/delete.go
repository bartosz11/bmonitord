package orchestrator

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"errors"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
	"strconv"
)

func HandleDeleteOrchestrator(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		param := c.Param("id")
		orchestratorId, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "orchestrator id")
			return
		}

		var orchestrator model.Orchestrator
		err = db.First(&orchestrator, "id = ?", orchestratorId).Error

		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		if orchestrator.Leader {
			// Even though orchestrators don't have any relations in the DB, it's still pretty bad to delete the one that's leader ig
			resp := helpers.HTTPResponse{
				Code:  http.StatusBadRequest,
				Error: "orchestrator cannot be leader",
			}
			resp.WriteAsJSON(c)
			return
		}

		err = db.Unscoped().Delete(&orchestrator).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusNoContent,
		}
		resp.WriteAsJSON(c)
	}
}
