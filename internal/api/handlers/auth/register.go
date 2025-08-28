package auth

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog/log"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
)

// HandleRegister docs
// @Summary Register
// @Description Allows a user to create a new account with the supplied credentials
// @Tags auth
// @Accept json
// @Produce json
// @Param credentials body Credentials true "Username and password for the new user account"
// @Success 201 {object} registerSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body does not match the requirements."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when supplied credentials are invalid."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when registration is disabled."
// @Failure 409 {object} helpers.GenericErrorResponse "Returned when specified username is already taken."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction or password hashing fails."
// @Router /auth/register [post]
func HandleRegister(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		var registrationEnabled model.Setting
		db.First(&registrationEnabled, "key = ?", "registration-enabled")
		if registrationEnabled.Value != nil {
			value, err := strconv.ParseBool(*registrationEnabled.Value)
			if err != nil {
				log.Err(err).Msg("failed to parse registration-enabled setting value to bool")
				//If a value exists but may have been mistyped in "manual SQL" or smth, let's assume the admin doesn't want more users
				registrationDisabled(c)
				return
			}
			if !value {
				registrationDisabled(c)
				return
			}
		} // If there's no setting created, probably the account who could've changed that doesn't exist yet :)

		registerReq := Credentials{}
		if err := c.ShouldBind(&registerReq); err != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
			return
		}

		if helpers.ContainsAnySpace(registerReq.Username) {
			helpers.BadRequestWithSpecificError(c, "username cannot contain any whitespace characters")
			return
		}

		if !errors.Is(db.First(&model.User{}, "username = ?", registerReq.Username).Error, gorm.ErrRecordNotFound) {
			helpers.UsernameAlreadyTaken(c)
			return
		}

		if !helpers.IsStrongPassword(registerReq.Password) {
			helpers.WeakPassword(c)
			return
		}
		//If there's no user yet, the first user gets admin perms
		admin := false
		var count int64
		db.Model(&model.User{}).Count(&count)
		if count == 0 {
			admin = true
		}

		hash, err := bcrypt.GenerateFromPassword([]byte(registerReq.Password), 12)
		if err != nil {
			log.Err(err).Msg("failed to hash password")
			helpers.PasswordHashingFailed(c)
			return
		}

		user := model.User{
			Username: registerReq.Username,
			Password: string(hash),
			Enabled:  true,
			Admin:    admin,
		}

		err = db.Create(&user).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusCreated,
			Data: user,
		}
		resp.WriteAsJSON(c)
	}
}

func registrationDisabled(c *gin.Context) {
	resp := helpers.HTTPResponse{
		Code:  http.StatusForbidden,
		Error: "registration is disabled",
	}
	resp.WriteAsJSON(c)
}

type registerSuccessResponse struct {
	Code int        `json:"code" example:"201"`
	Data model.User `json:"data"`
}
