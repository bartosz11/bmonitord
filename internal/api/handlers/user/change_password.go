package user

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog/log"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
	"net/http"
)

func HandleChangePassword(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		changePasswordReq := struct {
			OldPassword string `json:"oldPassword" binding:"required"`
			NewPassword string `json:"newPassword" binding:"required"`
		}{}

		if err := c.ShouldBind(&changePasswordReq); err != nil {
			helpers.BadRequest(c)
			return
		}

		if !helpers.IsStrongPassword(changePasswordReq.NewPassword) {
			helpers.WeakPassword(c)
			return
		}

		value, _ := c.Get("user")
		user := value.(model.User)

		if bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(changePasswordReq.OldPassword)) != nil {
			resp := helpers.HTTPResponse{
				Code:  http.StatusForbidden,
				Error: "invalid old password",
			}
			resp.WriteAsJSON(c)
			return
		}

		hash, err := bcrypt.GenerateFromPassword([]byte(changePasswordReq.NewPassword), 12)
		if err != nil {
			log.Err(err).Msg("failed to hash password")
			helpers.PasswordHashingFailed(c)
			return
		}

		user.Password = string(hash)
		err = db.Save(&user).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: user,
		}
		resp.WriteAsJSON(c)
	}
}
