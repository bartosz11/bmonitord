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

// HandleGetAllAlarmsByTargetID docs
// @Summary Get all alarms of a target
// @Description Allows a user to get a list of alarms that belong to a target
// @Tags alarm
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param targetID path uint true "ID of target"
// @Success 200 {object} listAlarmsSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given target ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a target with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /target/:targetID/alarm/ [get]
func HandleGetAllAlarmsByTargetID(db *gorm.DB) gin.HandlerFunc {
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

		var alarms []model.Alarm
		err = db.Preload("Notifications").Find(&alarms, "target_id = ?", target.ID).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		response := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: alarms,
		}
		response.WriteAsJSON(c)
	}
}

type listAlarmsSuccessResponse struct {
	Code int           `json:"code" example:"200"`
	Data []model.Alarm `json:"data"`
}
