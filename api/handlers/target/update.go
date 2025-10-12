package target

import (
	"errors"
	"net/http"
	"strconv"
	"strings"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleUpdateTargetById docs
// @Summary Update target
// @Description Allows a user to change information about a target
// @Tags target
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param targetID path uint true "ID of target to update"
// @Param updateReq body UpdateTargetRequest true "New information about target"
// @Success 200 {object} getTargetSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given ID couldn't be parsed or request body doesn't match the requirements."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a target with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /target/{targetID} [patch]
func HandleUpdateTargetById(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("targetID")
		id, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "target id")
			return
		}

		var target model.Target
		err = db.Joins("HTTPInfo").Joins("PingInfo").First(&target, "targets.id = ? and user_id = ?", id, user.ID).Error

		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		updateRequest := UpdateTargetRequest{}
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

		if db.Session(&gorm.Session{FullSaveAssociations: true}).Save(&target).Error != nil {
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

type UpdateTargetRequest struct {
	// Name must not be blank if supplied
	Name       *string `json:"name,omitempty"`
	MaxRetries *uint   `json:"maxRetries,omitempty"`
	Timeout    *uint   `json:"timeout,omitempty"`
	// Must contain at least one checker ID, all checkers must exist. This is a "replace update"
	CheckerIDs *[]uint                `json:"checkerIDs,omitempty"`
	PingInfo   *PingInfoUpdateRequest `json:"pingInfo,omitempty"`
	HTTPInfo   *HTTPInfoUpdateRequest `json:"httpInfo,omitempty"`
}

type HTTPInfoUpdateRequest struct {
	// Host must start with http:// or https:// if supplied
	Host *string `json:"host,omitempty"`
	// HTTP response codes separated by a space. Must contain at least one code if supplied. All codes must be exactly 3 digits long.
	AllowedCodes    *string `json:"allowedCodes,omitempty"`
	FollowRedirects *bool   `json:"followRedirects,omitempty"`
	VerifySSLCert   *bool   `json:"verifySSLCert,omitempty"`
}

type PingInfoUpdateRequest struct {
	// Host must not be blank if pingInfo is supplied in UpdateTargetRequest
	Host *string `json:"host,omitempty"`
}
