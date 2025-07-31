package auth

import (
	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog/log"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
	"net/http"
	"time"
)

type Credentials struct {
	// Username cannot be blank
	Username string `json:"username" binding:"required"`
	// Password must contain at least 1 uppercase letter, 1 lowercase letter, 1 number and must consist of at least 8 characters.
	Password string `json:"password" binding:"required"`
}

// HandleLogin docs
// @Summary Log in
// @Description Allows a user with valid credentials to create a new login session and a token.
// @Tags auth
// @Accept json
// @Produce json
// @Param credentials body Credentials true "Valid credentials of user who wants to create a new session"
// @Success 200 {object} loginSuccessResponse
// @Header 200 {string} Set-Cookie "Authentication cookie (auth-token=...)"
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body does not match the requirements."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when supplied credentials are invalid."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when the user account is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction or token generation fails."
// @Router /auth/login [post]
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

// loginSuccessResponse Such structs exist around the codebase only for Swagger doc purposes, they represent the structure of an API response
type loginSuccessResponse struct {
	Code int `json:"code" example:"200"`
	Data struct {
		Session model.Session `json:"session"`
		Token   string        `json:"token"`
	} `json:"data"`
}
