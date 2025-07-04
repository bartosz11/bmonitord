package settings

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

func HandleGetAllSettings(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		var settings []model.Setting
		err := db.Find(&settings).Error

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		c.JSON(http.StatusOK, gin.H{
			"settings": settings,
		})
	}
}
