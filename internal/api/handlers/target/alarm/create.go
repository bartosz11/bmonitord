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

func HandleCreateAlarm(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("targetID")
		targetID, err := strconv.ParseUint(param, 10, 64)
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

		createReq := struct {
			Name            string                     `json:"name" binding:"required"`
			Type            model.AlarmType            `json:"type" binding:"requiredUint"`
			NotificationIDs []uint                     `json:"notificationIDs" binding:"required,min=1"`
			Threshold       *float64                   `json:"threshold"`
			ThresholdField  *model.AlarmThresholdField `json:"thresholdField"`
		}{}

		if c.ShouldBind(&createReq) != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
			return
		}

		alarm := model.Alarm{
			TargetID: target.ID,
		}

		if helpers.IsBlank(createReq.Name) {
			helpers.BadRequestWithSpecificError(c, "alarm name cannot be blank")
			return
		}
		alarm.Name = createReq.Name

		if err = model.ValidateAlarmType(createReq.Type); err != nil {
			helpers.BadRequestWithSpecificError(c, err.Error())
			return
		}
		alarm.Type = createReq.Type

		if createReq.Type == model.Threshold {
			if createReq.Threshold == nil {
				helpers.BadRequestWithSpecificError(c, "threshold not supplied for threshold type alarm")
				return
			}
			//I don't think it's my problem if user sets the threshold to a negative value or bs like that
			alarm.Threshold = *createReq.Threshold

			if createReq.ThresholdField == nil {
				helpers.BadRequestWithSpecificError(c, "threshold field not supplied for threshold type alarm")
				return
			}

			if err = model.ValidateAlarmThresholdField(*createReq.ThresholdField); err != nil {
				helpers.BadRequestWithSpecificError(c, err.Error())
				return
			}
			alarm.ThresholdField = *createReq.ThresholdField
		}

		var notifications []model.Notification
		err = db.Find(&notifications, "id in ? and user_id = ?", createReq.NotificationIDs, user.ID).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		if len(createReq.NotificationIDs) != len(notifications) {
			helpers.BadRequestWithSpecificError(c, "at least one of specified notifications couldn't be found")
			return
		}
		alarm.Notifications = notifications

		err = db.Save(&alarm).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		response := helpers.HTTPResponse{
			Code: http.StatusCreated,
			Data: alarm,
		}
		response.WriteAsJSON(c)
	}
}
