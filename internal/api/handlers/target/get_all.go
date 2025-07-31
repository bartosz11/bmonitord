package target

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

// HandleGetAllUsersTargets docs
// @Summary Get all targets
// @Description Allows a user to get a list of their targets
// @Tags target
// @Security BearerAuth
// @Accept json
// @Produce json
// @Success 200 {object} listTargetsSuccessResponse
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /target/ [get]
func HandleGetAllUsersTargets(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		var targets []model.Target
		db.Joins("HTTPInfo", "PingInfo").Preload("Checkers").Preload("Alarms").
			Find(&targets, "user_id = ?", user.ID)

		SanitizeTargets(&targets)
		response := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: targets,
		}
		response.WriteAsJSON(c)
	}
}

type listTargetsSuccessResponse struct {
	Code int            `json:"code" example:"200"`
	Data []model.Target `json:"data"`
}
