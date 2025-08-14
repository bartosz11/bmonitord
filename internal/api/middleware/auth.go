package middleware

import (
	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
	"strings"
	"time"
)

func AuthMiddleware(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		var tokenStr string

		authHeader := c.GetHeader("Authorization")
		if strings.HasPrefix(authHeader, "Bearer ") {
			tokenStr = strings.TrimPrefix(tokenStr, "Bearer ")
		}

		cookie, err := c.Cookie("auth-token")
		if err == nil {
			tokenStr = cookie
		}

		if tokenStr == "" {
			abortAuth(c)
			return
		}

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

		session.LastActive = time.Now()
		session.IpAddress = c.ClientIP()
		if db.Save(&session).Error != nil {
			// A more "honest" error, but we still should abort
			helpers.DBInteractionFailed(c)
			c.Abort()
			return
		}

		c.Set("user", session.User)
		c.Set("session", session)
		c.Next()
	}
}

func abortAuth(c *gin.Context) {
	resp := helpers.HTTPResponse{
		Code:  http.StatusUnauthorized,
		Error: "invalid auth token",
	}
	resp.AbortWithJSON(c)
}
