package middleware

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
	"strings"
	"time"
)

func AuthMiddleware(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		tokenStr := c.GetHeader("Authorization")
		if len(tokenStr) < 7 || !strings.HasPrefix(tokenStr, "Bearer ") {
			abortAuth(c)
			return
		}
		tokenStr = strings.TrimPrefix(tokenStr, "Bearer ")
		token, ok := helpers.ParseToken(tokenStr)
		if !ok || !token.Valid {
			abortAuth(c)
			return
		}

		_, sessionID, ok := helpers.ExtractIDsFromJWT(token)
		if !ok {
			abortAuth(c)
			return
		}

		var session model.Session
		if err := db.Joins("User").First(&session, "sessions.id = ?", sessionID).Error; err != nil {
			abortAuth(c)
			return
		}

		if time.Now().After(session.ExpiresAt) {
			//Delete expired sessions "on the spot" - I kinda don't want to build a task for that
			db.Unscoped().Delete(&session)
			abortAuth(c)
			return
		}

		if issuedAt, err := token.Claims.GetIssuedAt(); err != nil || issuedAt.Before(session.User.UpdatedAt) {
			db.Unscoped().Delete(&session)
			abortAuth(c)
			return
		}

		if !session.User.Enabled {
			helpers.AccountDisabled(c)
			return
		}

		c.Set("user", session.User)
		c.Set("session", session)
		c.Next()
	}
}

func abortAuth(c *gin.Context) {
	c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{
		"error": "invalid auth token",
	})
}
