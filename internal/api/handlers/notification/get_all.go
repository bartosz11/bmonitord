package notification

import (
	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

// HandleGetUsersNotifications docs
// @Summary Get all notifications
// @Description Allows a user to retrieve a list of all their notifications
// @Tags notification
// @Security BearerAuth
// @Accept json
// @Produce json
// @Success 200 {object} listNotificationsSuccessResponse
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /notification/ [get]
func HandleGetUsersNotifications(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		var notifications []model.Notification
		err := db.Find(&notifications, "user_id = ?", user.ID).Error

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		response := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: notifications,
		}
		response.WriteAsJSON(c)
	}
}

type listNotificationsSuccessResponse struct {
	Code int `json:"code" example:"200"`
	Data []model.Notification
}
