package notification

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

// HandleCreateNotification docs
// @Summary Create a notification
// @Description Allows a user to create a notification
// @Tags notification
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param createReq body CreateNotificationRequest true "Information about new notification"
// @Success 201 {object} createNotificationSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body doesn't match requirements."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /notification/ [post]
func HandleCreateNotification(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		notificationCreateReq := CreateNotificationRequest{}

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

type CreateNotificationRequest struct {
	// Name must not be blank
	Name string `json:"name" binding:"required"`
	// Type must be a number in range 0-5, inclusive
	Type model.NotificationType `json:"type" binding:"required"`
	// Credentials must be valid for the selected type, e.g. webhook URLs have to start with http:// or https://
	Credentials string `json:"credentials" binding:"required"`
}

type createNotificationSuccessResponse struct {
	Code int                `json:"code" example:"201"`
	Data model.Notification `json:"data"`
}
