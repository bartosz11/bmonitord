package notification

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleUpdateNotificationById docs
// @Summary Update notification
// @Description Allows a user to update a notification with specified ID
// @Tags notification
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param id path uint true "ID of notification to update"
// @Param updateReq body UpdateNotificationRequest true "Changes to make to the notification"
// @Success 200 {object} getNotificationSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given ID couldn't be parsed or request body doesn't match requirements."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a notification with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /notification/{id} [patch]
func HandleUpdateNotificationById(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		updateReq := UpdateNotificationRequest{}

		err := c.ShouldBind(&updateReq)
		if err != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
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

type UpdateNotificationRequest struct {
	// Name must not be blank if supplied
	Name *string `json:"name,omitempty"`
	// Type must be a number in range of 0-5 (inclusive), if supplied. If type is changed, credentials valid for the new type also have to be supplied.
	Type *model.NotificationType `json:"type,omitempty"`
	// Credentials must be valid for the selected type, e.g. webhook URLs have to start with http:// or https://, if supplied
	Credentials *string `json:"credentials,omitempty"`
}
