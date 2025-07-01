package session

import (
	"bmonitord/internal/api/helpers"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

func HandleLogout(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		session, _ := c.Get("session")
		err := db.Unscoped().Delete(&session).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		c.JSON(http.StatusNoContent, gin.H{})
	}
}
