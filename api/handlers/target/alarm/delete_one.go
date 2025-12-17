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

// HandleDeleteAlarmByID docs
// @Summary Delete alarm by ID
// @Description Allows a user to delete an alarm with specified ID
// @Tags alarm
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param targetID path uint true "ID of target the alarm belongs to"
// @Param alarmID path uint true "ID of the alarm"
// @Success 204 {object} helpers.GenericDeleteSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given target or alarm ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a target or alarm with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /target/{targetID}/alarm/{alarmID} [delete]
func HandleDeleteAlarmByID(db *gorm.DB) gin.HandlerFunc {
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

		if alarm.System {
			resp := helpers.HTTPResponse{
				Code:  http.StatusBadRequest,
				Error: "system alarms cannot be deleted",
			}
			resp.WriteAsJSON(c)
			return
		}

		if db.Unscoped().Delete(&alarm).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		response := helpers.HTTPResponse{
			Code: http.StatusNoContent,
		}
		response.WriteAsJSON(c)
	}
}
