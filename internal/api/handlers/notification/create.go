package notification

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

func HandleCreateNotification(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		notificationCreateReq := struct {
			Name        string                 `json:"name" binding:"required"`
			Type        model.NotificationType `json:"type" binding:"required"`
			Credentials string                 `json:"credentials" binding:"required"`
		}{}

		if c.ShouldBind(&notificationCreateReq) != nil {
			helpers.BadRequestWithSpecificError(c, "parsing body failed")
			return
		}

		if helpers.IsBlank(notificationCreateReq.Name) {
			helpers.BadRequestWithSpecificError(c, "notification name must not be blank")
			return
		}

		if err := ValidateCredentialsForType(notificationCreateReq.Type, notificationCreateReq.Credentials); err != nil {
			helpers.BadRequestWithSpecificError(c, err.Error())
			return
		}
		normalizedCreds := NormalizeCredentials(notificationCreateReq.Type, notificationCreateReq.Credentials)

		notification := model.Notification{
			Name:        notificationCreateReq.Name,
			Type:        notificationCreateReq.Type,
			Credentials: normalizedCreds,
			UserID:      user.ID,
		}

		if db.Save(&notification).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		response := helpers.HTTPResponse{
			Code: http.StatusCreated,
			Data: notification,
		}
		response.WriteAsJSON(c)
	}
}
