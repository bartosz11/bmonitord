package notification

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"bmonitord/internal/notificationproviders"
	"errors"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
	"strconv"
)

var testNotificationPayload = notificationproviders.NotificationPayload{
	Header: "Test notification",
	Body:   "This is a test notification from bmonitord.",
}

func HandleSendTestNotification(db *gorm.DB) gin.HandlerFunc {
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

		go notificationproviders.NotificationProviders[notification.Type](testNotificationPayload, notification.Credentials)

		response := helpers.HTTPResponse{
			Code: http.StatusNoContent,
		}
		response.WriteAsJSON(c)
	}
}
