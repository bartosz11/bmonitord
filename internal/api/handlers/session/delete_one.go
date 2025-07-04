package session

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
	"strconv"
)

func HandleDeleteSession(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("id")
		id, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "session id")
			return
		}

		var count int64
		err = db.Model(&model.Session{}).Where("user_id = ? and id = ?", user.ID, id).Count(&count).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}
		if count == 0 {
			helpers.NotFound(c)
			return
		}

		err = db.Unscoped().Delete(&model.Session{}, "user_id = ? and id = ?", user.ID, id).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusNoContent,
		}
		resp.WriteAsJSON(c)
	}
}
