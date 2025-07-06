package notification

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"errors"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
	"strconv"
)

func HandleUpdateNotificationById(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		updateReq := struct {
			Name        *string                 `json:"name,omitempty"`
			Type        *model.NotificationType `json:"type,omitempty"`
			Credentials *string                 `json:"credentials,omitempty"`
		}{}

		err := c.ShouldBind(&updateReq)
		if err != nil {
			helpers.BadRequest(c)
			return
		}

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

		if updateReq.Name != nil {
			if helpers.IsBlank(*updateReq.Name) {
				helpers.BadRequestWithSpecificError(c, "name must not be blank if supplied")
				return
			}
			notification.Name = *updateReq.Name
		}

		if updateReq.Type != nil {
			if updateReq.Credentials == nil {
				helpers.BadRequestWithSpecificError(c, "new credentials must be supplied if type is changed")
				return
			}
			notification.Type = *updateReq.Type
		}

		if updateReq.Credentials != nil {
			if err = ValidateCredentialsForType(notification.Type, *updateReq.Credentials); err != nil {
				helpers.BadRequestWithSpecificError(c, err.Error())
				return
			}
			notification.Credentials = NormalizeCredentials(notification.Type, *updateReq.Credentials)
		}

		if db.Save(&notification).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		response := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: notification,
		}
		response.WriteAsJSON(c)
	}
}
