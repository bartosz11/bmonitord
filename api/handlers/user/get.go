package user

import (
	"net/http"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
)

// HandleGetCurrentUser docs
// @Summary Get current user
// @Description Allows user to get information about their own account
// @Tags user
// @Security BearerAuth
// @Accept json
// @Produce json
// @Success 200 {object} getUserSuccessResponse
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /user/ [get]
func HandleGetCurrentUser() gin.HandlerFunc {
	return func(c *gin.Context) {
		user, _ := c.Get("user")
		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: user,
		}
		resp.WriteAsJSON(c)
	}
}

type getUserSuccessResponse struct {
	Code int        `json:"code" example:"200"`
	Data model.User `json:"data"`
}
