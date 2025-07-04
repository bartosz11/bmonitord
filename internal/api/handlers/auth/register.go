package auth

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"errors"
	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog/log"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
	"net/http"
	"strconv"
)

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
			if value {
				registrationDisabled(c)
				return
			}
		} // If there's no setting created, probably the account who could've changed that doesn't exist yet :)

		registerReq := Credentials{}
		if err := c.ShouldBind(&registerReq); err != nil {
			helpers.BadRequest(c)
			return
		}

		if helpers.ContainsAnySpace(registerReq.Username) {
			helpers.BadRequest(c)
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
