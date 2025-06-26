package auth

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog/log"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
	"net/http"
	"time"
)

type Credentials struct {
	Username string `json:"username"`
	Password string `json:"password"`
}

func HandleLogin(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		loginReq := Credentials{}
		if err := c.ShouldBind(&loginReq); err != nil {
			helpers.BadRequest(c)
			return
		}

		var user model.User
		if db.First(&user, "username = ?", loginReq.Username).Error != nil {
			invalidUsernameOrPassword(c)
			return
		}

		if !user.Enabled {
			helpers.AccountDisabled(c)
			return
		}

		if bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(loginReq.Password)) != nil {
			invalidUsernameOrPassword(c)
			return
		}

		session := model.Session{
			ExpiresAt:  time.Now().Add(time.Minute * time.Duration(helpers.JWTValidity)),
			LastActive: time.Now(),
			UserAgent:  c.GetHeader("User-Agent"),
			IpAddress:  c.ClientIP(),
			UserID:     user.ID,
		}
		db.Create(&session)

		jwt, err := helpers.GenerateJWT(user.ID, session.ID)
		if err != nil {
			log.Err(err).Msg("failed to generate token")
			c.JSON(http.StatusInternalServerError, gin.H{
				"error": "failed to generate token",
			})
			return
		}

		//TODO: un-hardcode secure later
		c.SetCookie("auth-token", jwt, helpers.JWTValidity*60, "/", "", false, false)
		c.JSON(200, gin.H{
			"session": session,
			"token":   jwt,
		})
	}
}

func invalidUsernameOrPassword(c *gin.Context) {
	c.JSON(http.StatusUnauthorized, gin.H{
		"error": "invalid username or password",
	})
}
