package session

import (
	"bmonitord/internal/api/helpers"
	"github.com/gin-gonic/gin"
	"net/http"
)

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
