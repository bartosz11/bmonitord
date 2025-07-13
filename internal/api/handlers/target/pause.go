package target

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"errors"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
	"strconv"
)

func HandlePauseTarget(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		value, _ := c.Get("user")
		user := value.(model.User)

		param := c.Param("targetID")
		id, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "target id")
			return
		}

		var target model.Target
		err = db.First(&target, "user_id = ? and id = ?", user.ID, id).Error

		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		pauseParam := c.Query("pause")
		if pauseParam == "" {
			//The endpoint behaves like a "toggle switch" if value isn't supplied at all (missing or empty param)
			target.Paused = !target.Paused
		} else {
			//Otherwise behave normally
			pause, err := strconv.ParseBool(pauseParam)
			if err != nil {
				helpers.ParsingFailed(c, "pause status")
				return
			} else {
				target.Paused = pause
			}
		}

		if db.Save(&target).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		//Just in case
		SanitizeTarget(&target)
		response := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: target,
		}
		response.WriteAsJSON(c)
	}
}
