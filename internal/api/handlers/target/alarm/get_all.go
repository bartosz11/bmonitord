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
