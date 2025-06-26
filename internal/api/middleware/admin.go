package middleware

import (
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"net/http"
)

func AdminMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		//If the auth middleware completed successfully (this one is next for selected endpoints) "user" has to exist and be of type model.User
		value, _ := c.Get("user")
		user := value.(model.User)

		if !user.Admin {
			c.AbortWithStatusJSON(http.StatusForbidden, gin.H{
				"error": "not admin",
			})
			return
		}

		c.Next()
	}
}
