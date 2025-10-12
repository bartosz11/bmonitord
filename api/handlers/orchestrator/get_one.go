package orchestrator

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleGetOrchestratorByID docs
// @Summary Get orchestrator by ID
// @Description Allows an admin to retrieve an orchestrator with given ID
// @Tags orchestrator
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param id path uint true "ID of orchestrator to get"
// @Success 200 {object} getOrchestratorSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when user sending the request is not an admin or their account is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when orchestrator with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /admin/orchestrator/{id} [get]
func HandleGetOrchestratorByID(db *gorm.DB) gin.HandlerFunc {
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

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: orchestrator,
		}
		resp.WriteAsJSON(c)
	}
}

type getOrchestratorSuccessResponse struct {
	Code int                `json:"code" example:"200"`
	Data model.Orchestrator `json:"data"`
}
