package orchestrator

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

// HandleGetAllOrchestrators docs
// @Summary Get all orchestrators
// @Description Allows an admin to get a list of all orchestrators
// @Tags orchestrator
// @Security BearerAuth
// @Accept json
// @Produce json
// @Success 200 {object} listOrchestratorsSuccessResponse
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when user sending the request is not an admin or their account is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /admin/orchestrator/ [get]
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

type listOrchestratorsSuccessResponse struct {
	Code int                  `json:"code" example:"200"`
	Data []model.Orchestrator `json:"data"`
}
