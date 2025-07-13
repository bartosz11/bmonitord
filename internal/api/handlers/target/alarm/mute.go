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

func HandleMuteAlarm(db *gorm.DB) gin.HandlerFunc {
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

		//This behavior is analogical to target pause endpoint
		muteParam := c.Query("mute")
		if muteParam == "" {
			alarm.Muted = !alarm.Muted
		} else {
			mute, err := strconv.ParseBool(muteParam)
			if err != nil {
				helpers.ParsingFailed(c, "mute status")
				return
			} else {
				alarm.Muted = mute
			}
		}

		if db.Save(&alarm).Error != nil {
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
