package alarm

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleUpdateAlarm docs
// @Summary Update alarm
// @Description Allows a user to update information about an alarm with specified ID
// @Tags alarm
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param targetID path uint true "ID of target the alarm belongs to"
// @Param alarmID path uint true "ID of the alarm"
// @Param updateReq body UpdateAlarmRequest true "New information about the alarm"
// @Success 200 {object} getAlarmSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given target or alarm ID couldn't be parsed or request body doesn't match the requirements."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a target or alarm with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /target/{targetID}/alarm/{alarmID} [patch]
func HandleUpdateAlarm(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		targetIDParam := c.Param("targetID")
		targetID, err := strconv.ParseUint(targetIDParam, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "target ID")
			return
		}

		var target model.Target
		err = db.First(&target, "id = ? and user_id = ?", targetID, user.ID).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		alarmIDParam := c.Param("alarmID")
		alarmID, err := strconv.ParseUint(alarmIDParam, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "alarm ID")
			return
		}

		var alarm model.Alarm
		err = db.First(&alarm, "target_id = ? and id = ?", target.ID, alarmID).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		updateReq := UpdateAlarmRequest{}

		if c.ShouldBind(&updateReq) != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
			return
		}

		if updateReq.Name != nil {
			if helpers.IsBlank(*updateReq.Name) {
				helpers.BadRequestWithSpecificError(c, "alarm name cannot be blank")
				return
			}
			alarm.Name = *updateReq.Name
		}

		if updateReq.Type != nil {
			if err = model.ValidateAlarmType(*updateReq.Type); err != nil {
				helpers.BadRequestWithSpecificError(c, err.Error())
				return
			}
			alarm.Type = *updateReq.Type
		}

		if alarm.Type == model.Threshold {
			if updateReq.Threshold != nil {
				alarm.Threshold = *updateReq.Threshold
			} else if updateReq.Type != nil {
				//This is the case where the alarm type is switched to threshold without required data
				helpers.BadRequestWithSpecificError(c, "threshold not supplied for threshold type alarm")
				return
			}

			if updateReq.ThresholdField != nil {
				if err = model.ValidateAlarmThresholdField(*updateReq.ThresholdField); err != nil {
					helpers.BadRequestWithSpecificError(c, err.Error())
					return
				}
				alarm.ThresholdField = *updateReq.ThresholdField
			} else if updateReq.Type != nil {
				helpers.BadRequestWithSpecificError(c, "threshold field not supplied for threshold type alarm")
				return
			}
		}

		if updateReq.NotificationIDs != nil {
			if len(*updateReq.NotificationIDs) < 1 {
				helpers.BadRequestWithSpecificError(c, "alarm should have at least 1 notification assigned")
				return
			}
			var notifications []model.Notification
			err = db.Find(&notifications, "id in ? and user_id = ?", *updateReq.NotificationIDs, user.ID).Error
			if err != nil {
				helpers.DBInteractionFailed(c)
				return
			}

			if len(*updateReq.NotificationIDs) != len(notifications) {
				helpers.BadRequestWithSpecificError(c, "at least one of specified notifications couldn't be found")
				return
			}

			if db.Model(&alarm).Association("Notifications").Replace(&notifications) != nil {
				helpers.DBInteractionFailed(c)
				return
			}
		}

		err = db.Save(&alarm).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		response := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: alarm,
		}
		response.WriteAsJSON(c)
	}
}

type UpdateAlarmRequest struct {
	// Name must not be blank if supplied
	Name *string `json:"name"`
	// Type must be 0 (unavailable) or 1 (threshold), if supplied
	Type *model.AlarmType `json:"type"`
	// min. length = 1 if supplied, all notifications must exist. This is a "replace update"
	NotificationIDs *[]uint `json:"notificationIDs"`
	// Must be supplied if type is getting changed to threshold (1)
	Threshold *float64 `json:"threshold"`
	// Must be supplied if type is getting changed to threshold (1), at the moment the only accepted value is 0 (latency)
	ThresholdField *model.AlarmThresholdField `json:"thresholdField"`
}
