package alarm

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleCreateAlarm docs
// @Summary Create alarm
// @Description Allows a user to create an alarm assigned to a specified target
// @Tags alarm
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param targetID path uint true "ID of target the alarm is supposed to belong to"
// @Param updateReq body CreateAlarmRequest true "Information about the new alarm"
// @Success 200 {object} createAlarmSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given target ID couldn't be parsed or request body doesn't match the requirements."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a target with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /target/{targetID}/alarm/ [post]
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

		createReq := CreateAlarmRequest{}

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

			if createReq.ThresholdFieldParams == nil {
				helpers.BadRequestWithSpecificError(c, "threshold field params not supplied for threshold type alarm")
				return
			}
			alarm.ThresholdFieldParams = *createReq.ThresholdFieldParams
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

type CreateAlarmRequest struct {
	// Name must not be blank
	Name string `json:"name" binding:"required"`
	// Type must be 0 (unavailable) or 1 (threshold)
	Type model.AlarmType `json:"type" binding:"requiredUint"`
	// All notifications in this list must exist
	NotificationIDs []uint `json:"notificationIDs" binding:"required,min=1"`
	// Threshold must be supplied if type is 1 (threshold)
	Threshold *float64 `json:"threshold"`
	// Threshold field must be supplied if type is 1 (threshold). At the moment the only accepted value is 0 (latency)
	ThresholdField *model.AlarmThresholdField `json:"thresholdField"`
	// Threshold field params must be supplied if type is 1 (threshold). Used to specify when to trigger alarms in some cases, can be left blank though
	ThresholdFieldParams *string `json:"thresholdFieldParams"`
}

type createAlarmSuccessResponse struct {
	Code int         `json:"code" example:"201"`
	Data model.Alarm `json:"data"`
}
