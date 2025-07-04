package user

import (
	"bmonitord/internal/api/helpers"
	"github.com/gin-gonic/gin"
	"net/http"
)

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
