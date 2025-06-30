package user

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

func HandleChangeUsername(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		changeUsernameReq := struct {
			NewUsername string `json:"newUsername" binding:"required"`
		}{}

		if err := c.ShouldBind(&changeUsernameReq); err != nil {
			helpers.BadRequest(c)
			return
		}

		if helpers.ContainsAnySpace(changeUsernameReq.NewUsername) {
			helpers.BadRequest(c)
			return
		}

		var count int64
		db.Model(&model.User{}).Where("username = ?", changeUsernameReq.NewUsername).Count(&count)
		if count != 0 {
			helpers.UsernameAlreadyTaken(c)
			return
		}

		//has to exist in restricted endpoints and has to be of type model.User, check auth.go if you don't believe me
		value, _ := c.Get("user")
		user := value.(model.User)

		user.Username = changeUsernameReq.NewUsername
		err := db.Save(&user).Error
		//That's what Spring would do, I guess
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		c.JSON(http.StatusOK, gin.H{
			"user": user,
		})
	}
}
