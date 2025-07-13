package alarm

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"errors"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
	"strconv"
)

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

		updateReq := struct {
			Name            *string                    `json:"name"`
			Type            *model.AlarmType           `json:"type"`
			NotificationIDs *[]uint                    `json:"notificationIDs"`
			Threshold       *float64                   `json:"threshold"`
			ThresholdField  *model.AlarmThresholdField `json:"thresholdField"`
		}{}

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
