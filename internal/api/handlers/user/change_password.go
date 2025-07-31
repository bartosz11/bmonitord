package user

import (
	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog/log"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
	"net/http"
)

// HandleChangePassword docs
// @Summary Change password
// @Description Allows user to change their own password
// @Tags user
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param changePasswordReq body ChangePasswordRequest true "Old and new password"
// @Success 200 {object} getUserSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body is malformed or new password is too weak."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled or provided old password doesn't match."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction or password hashing fails."
// @Router /user/password [patch]
func HandleChangePassword(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		changePasswordReq := ChangePasswordRequest{}

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

type ChangePasswordRequest struct {
	OldPassword string `json:"oldPassword" binding:"required"`
	// New password must contain at least 1 uppercase letter, 1 lowercase letter, 1 number and must consist of at least 8 characters.
	NewPassword string `json:"newPassword" binding:"required"`
}
