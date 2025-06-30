package user

import (
	"github.com/gin-gonic/gin"
	"net/http"
)

func HandleGetCurrentUser() gin.HandlerFunc {
	return func(c *gin.Context) {
		user, _ := c.Get("user")
		c.JSON(http.StatusOK, gin.H{
			"user": user,
		})
	}
}
