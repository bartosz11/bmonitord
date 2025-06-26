package auth

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"errors"
	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog/log"
	"gorm.io/gorm"
	"net/http"
	"regexp"
	"strconv"
)

var PasswordValidationRegex = regexp.MustCompile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")

func HandleRegister(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		var registrationEnabled model.Setting
		db.First(&registrationEnabled, "key = registration-enabled")
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

		if !errors.Is(db.First(&model.User{}, "username = ?", registerReq.Username).Error, gorm.ErrRecordNotFound) {
			c.JSON(http.StatusConflict, gin.H{
				"error": "username already taken",
			})
			return
		}

		if !PasswordValidationRegex.MatchString(registerReq.Password) {
			c.JSON(http.StatusBadRequest, gin.H{
				"error": "invalid password: password must contain at least 1 uppercase letter, 1 lowercase letter, 1 number and must consist of at least 8 characters.",
			})
			return
		}
		//If there's no user yet, the first user gets admin perms
		admin := false
		var count int64
		db.Model(&model.User{}).Count(&count)
		if count == 0 {
			admin = true
		}

		user := model.User{
			Username: registerReq.Username,
			Password: registerReq.Password,
			Enabled:  true,
			Admin:    admin,
		}
		db.Create(&user)

		c.JSON(http.StatusCreated, gin.H{
			"user": user,
		})
	}
}

func registrationDisabled(c *gin.Context) {
	c.JSON(http.StatusForbidden, gin.H{
		"error": "registration is disabled",
	})
}
