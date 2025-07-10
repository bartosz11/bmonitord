package target

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"errors"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
	"strconv"
	"strings"
)

func HandleUpdateTargetById(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("id")
		id, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "target id")
			return
		}

		var target model.Target
		err = db.Joins("HTTPInfo", "PingInfo").First(&target, "targets.id = ? and user_id = ?", id, user.ID).Error

		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		updateRequest := UpdateRequest{}
		if c.ShouldBind(&updateRequest) != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
		}

		newName := updateRequest.Name
		if newName != nil {
			if helpers.IsBlank(*newName) {
				helpers.BadRequestWithSpecificError(c, "name must not be blank if specified for update")
				return
			}
			target.Name = *newName
		}

		if updateRequest.MaxRetries != nil {
			target.MaxRetries = *updateRequest.MaxRetries
		}

		if updateRequest.Timeout != nil {
			target.Timeout = *updateRequest.Timeout
		}

		if updateRequest.CheckerIDs != nil {
			if len(*updateRequest.CheckerIDs) < 1 {
				helpers.BadRequestWithSpecificError(c, "target must be associated with at least one checker")
				return
			}
			//I really will need to do a huge refactoring at some point, a metric fuckton of code is just straight up duplicated
			var checkers []model.Checker
			err = db.Find(&checkers, *updateRequest.CheckerIDs).Error
			if err != nil {
				helpers.DBInteractionFailed(c)
				return
			}

			if len(checkers) != len(*updateRequest.CheckerIDs) {
				helpers.BadRequestWithSpecificError(c, "at least one of specified checkers hasn't been found")
				return
			}

			if db.Model(&target).Association("Checkers").Replace(&checkers) != nil {
				helpers.DBInteractionFailed(c)
				return
			}
		}

		//there's nothing else in ping to update (as of now at least), hence the last check
		if target.Type == model.PING && updateRequest.PingInfo != nil && updateRequest.PingInfo.Host != nil {
			if helpers.IsBlank(*updateRequest.PingInfo.Host) {
				helpers.BadRequestWithSpecificError(c, "ping host host cannot be blank")
				return
			}
			target.PingInfo.Host = *updateRequest.PingInfo.Host
		}

		if target.Type == model.HTTP && updateRequest.HTTPInfo != nil {
			if updateRequest.HTTPInfo.Host != nil {
				if !strings.HasPrefix(*updateRequest.HTTPInfo.Host, "https://") && !strings.HasPrefix(*updateRequest.HTTPInfo.Host, "http://") {
					helpers.BadRequestWithSpecificError(c, "HTTP host must start with http:// or https://")
					return
				}
				target.HTTPInfo.Host = *updateRequest.HTTPInfo.Host
			}

			if updateRequest.HTTPInfo.AllowedCodes != nil {
				if err = ValidateAllowedCodes(*updateRequest.HTTPInfo.AllowedCodes); err != nil {
					helpers.BadRequestWithSpecificError(c, err.Error())
					return
				}
				target.HTTPInfo.AllowedCodes = *updateRequest.HTTPInfo.AllowedCodes
			}

			if updateRequest.HTTPInfo.VerifySSLCert != nil {
				target.HTTPInfo.VerifySSLCert = *updateRequest.HTTPInfo.VerifySSLCert
			}

			if updateRequest.HTTPInfo.FollowRedirects != nil {
				target.HTTPInfo.FollowRedirects = *updateRequest.HTTPInfo.FollowRedirects
			}
		}

		if db.Save(&target).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		SanitizeTarget(&target)
		response := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: target,
		}
		response.WriteAsJSON(c)
	}
}

type UpdateRequest struct {
	Name       *string                `json:"name,omitempty"`
	MaxRetries *uint                  `json:"maxRetries,omitempty"`
	Timeout    *uint                  `json:"timeout,omitempty"`
	CheckerIDs *[]uint                `json:"checkerIDs,omitempty"`
	PingInfo   *PingInfoUpdateRequest `json:"pingInfo,omitempty"`
	HTTPInfo   *HTTPInfoUpdateRequest `json:"httpInfo,omitempty"`
}

type HTTPInfoUpdateRequest struct {
	Host            *string `json:"host,omitempty"`
	AllowedCodes    *string `json:"allowedCodes,omitempty"`
	FollowRedirects *bool   `json:"followRedirects,omitempty"`
	VerifySSLCert   *bool   `json:"verifySSLCert,omitempty"`
}

type PingInfoUpdateRequest struct {
	Host *string `json:"host,omitempty"`
}
