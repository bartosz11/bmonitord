package orchestrator

import (
	"errors"
	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
	"strconv"
)

// HandleDeleteOrchestrator docs
// @Summary Delete orchestrator
// @Description Allows an admin to delete an orchestrator with given ID
// @Tags orchestrator
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param id path uint true "ID of orchestrator that should get deleted"
// @Success 204 {object} helpers.GenericDeleteSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given ID couldn't be parsed or specified orchestrator is currently leader."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when user sending the request is not an admin or their account is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when orchestrator with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /admin/orchestrator/:id [delete]
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
