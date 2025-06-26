package api

import (
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
		token, ok := ParseToken(tokenStr)
		if !ok || !token.Valid {
			abortAuth(c)
			return
		}

		userID, sessionID, ok := ExtractIDsFromJWT(token)
		if !ok {
			abortAuth(c)
			return
		}

		var session model.Session
		if err := db.First(&session, "id = ?", sessionID).Error; err != nil {
			abortAuth(c)
			return
		}

		if time.Now().After(session.ExpiresAt) {
			//Delete expired sessions "on the spot" - I kinda don't want to build a task for that
			db.Unscoped().Delete(&session)
			abortAuth(c)
			return
		}

		c.Set("userID", userID)
		c.Set("sessionID", sessionID)
		c.Next()
	}
}

func abortAuth(c *gin.Context) {
	c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{
		"error": "invalid auth token",
	})
}
