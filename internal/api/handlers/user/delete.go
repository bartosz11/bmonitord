package user

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

func HandleDeleteCurrentUser(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		value, _ := c.Get("user")
		user := value.(model.User)

		//There's pretty much no point in soft deleting anything other than checkers
		//since everything "DELETE CASCADE"s I can just do this:
		err := db.Unscoped().Delete(&user).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}
		c.JSON(http.StatusNoContent, gin.H{})
	}
}
