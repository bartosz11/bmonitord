package target

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

func HandleGetAllUsersTargets(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		var targets []model.Target
		db.Joins("HTTPInfo", "PingInfo").Preload("Checkers").Preload("Alarms").
			Find(&targets, "user_id = ?", user.ID)

		SanitizeTargets(&targets)
		response := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: targets,
		}
		response.WriteAsJSON(c)
	}
}
