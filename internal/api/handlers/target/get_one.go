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

func HandleGetTargetByID(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("id")
		id, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "target id")
			return
		}

		var target model.Target
		err = db.Joins("HTTPInfo", "PingInfo").Preload("Checkers").Preload("Alarms").
			First(&target, "targets.id = ? and user_id = ?", id, user.ID).Error

		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		SanitizeTarget(&target)
		response := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: target,
		}
		response.WriteAsJSON(c)
	}
}
