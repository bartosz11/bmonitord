package notification

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleDeleteNotificationById docs
// @Summary Delete a notification by ID
// @Description Allows a user to delete a notification with specified ID
// @Tags notification
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param id path uint true "ID of notification to be deleted"
// @Success 204 {object} helpers.GenericDeleteSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a notification with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /notification/{id} [delete]
func HandleDeleteNotificationById(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		param := c.Param("id")
		id, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "notification id")
			return
		}

		val, _ := c.Get("user")
		user := val.(model.User)

		var notification model.Notification
		err = db.First(&notification, "user_id = ? and id = ?", user.ID, id).Error

		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		if db.Unscoped().Delete(&notification).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		response := helpers.HTTPResponse{
			Code: http.StatusNoContent,
		}
		response.WriteAsJSON(c)
	}
}
