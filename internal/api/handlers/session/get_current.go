package session

import (
	"github.com/gin-gonic/gin"
	"net/http"
)

func HandleGetCurrentSession() gin.HandlerFunc {
	return func(c *gin.Context) {
		session, _ := c.Get("session")
		c.JSON(http.StatusOK, gin.H{
			"session": session,
		})
	}
}
