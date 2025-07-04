package checker

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

func HandleGetAllCheckers(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		var checkers []model.Checker

		if db.Find(&checkers).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		c.JSON(http.StatusOK, gin.H{
			"checkers": checkers,
		})
	}
}
