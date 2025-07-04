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
	Username string `json:"username" binding:"required"`
	Password string `json:"password" binding:"required"`
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
			User:       user,
		}

		err := db.Create(&session).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		jwt, err := helpers.GenerateJWT(user.ID, session.ID)
		if err != nil {
			log.Err(err).Msg("failed to generate token")
			resp := helpers.HTTPResponse{
				Code:  http.StatusInternalServerError,
				Error: "failed to generate token",
			}
			resp.WriteAsJSON(c)
			return
		}

		//TODO: un-hardcode secure later
		c.SetCookie("auth-token", jwt, helpers.JWTValidity*60, "/", "", false, false)
		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: gin.H{
				"session": session,
				"token":   jwt,
			},
		}
		resp.WriteAsJSON(c)
	}
}

func invalidUsernameOrPassword(c *gin.Context) {
	resp := helpers.HTTPResponse{
		Code:  http.StatusUnauthorized,
		Error: "invalid username or password",
	}
	resp.WriteAsJSON(c)
}
