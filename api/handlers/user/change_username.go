package user

import (
	"net/http"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleChangeUsername docs
// @Summary Change username
// @Description Allows user to change their own username
// @Tags user
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param changeUsernameReq body ChangeUsernameRequest true "New username"
// @Success 200 {object} getUserSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body is malformed or new username contains a whitespace character."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 409 {object} helpers.GenericErrorResponse "Returned when given new username is already taken."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /user/username [patch]
func HandleChangeUsername(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		changeUsernameReq := ChangeUsernameRequest{}

		if err := c.ShouldBind(&changeUsernameReq); err != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
			return
		}

		if helpers.ContainsAnySpace(changeUsernameReq.NewUsername) {
			helpers.BadRequestWithSpecificError(c, "new username cannot contain any whitespace characters")
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

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: user,
		}
		resp.WriteAsJSON(c)
	}
}

type ChangeUsernameRequest struct {
	// Must not be blank and must not contain any whitespace characters
	NewUsername string `json:"newUsername" binding:"required"`
}
