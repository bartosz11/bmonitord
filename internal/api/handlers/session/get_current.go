package session

import (
	"bmonitord/internal/api/helpers"
	"github.com/gin-gonic/gin"
	"net/http"
)

// HandleGetCurrentSession docs
// @Summary Get current session
// @Description Allows a user to retrieve information about session assigned to supplied auth token.
// @Tags session
// @Security BearerAuth
// @Accept json
// @Produce json
// @Success 200 {object} getSessionSuccessResponse
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /session/current [get]
func HandleGetCurrentSession() gin.HandlerFunc {
	return func(c *gin.Context) {
		session, _ := c.Get("session")
		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: session,
		}
		resp.WriteAsJSON(c)
	}
}
