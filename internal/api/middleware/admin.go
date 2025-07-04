package middleware

import (
	"bmonitord/internal/api/helpers"
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
			resp := helpers.HTTPResponse{
				Code:  http.StatusForbidden,
				Error: "not admin",
			}
			resp.AbortWithJSON(c)
			return
		}

		c.Next()
	}
}
