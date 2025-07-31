package target

import (
	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
	"strings"
)

// HandleCreateTarget docs
// @Summary Create a target
// @Description Allows a user to create a target
// @Tags target
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param createReq body CreateTargetRequest true "Information about new target"
// @Success 201 {object} createTargetSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body doesn't match requirements."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /target/ [post]
func HandleCreateTarget(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		createReq := CreateTargetRequest{}

		err := c.ShouldBind(&createReq)
		if err != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
			return
		}

		if helpers.IsBlank(createReq.Name) {
			helpers.BadRequestWithSpecificError(c, "target name must not be blank")
			return
		}

		if err := model.ValidateTargetType(createReq.Type); err != nil {
			helpers.BadRequestWithSpecificError(c, err.Error())
			return
		}

		target := model.Target{
			Name:       createReq.Name,
			MaxRetries: createReq.MaxRetries,
			Type:       createReq.Type,
			Timeout:    createReq.Timeout,
			UserID:     user.ID,
		}

		if createReq.Type == model.HTTP {
			if createReq.HTTPInfo == nil {
				helpers.BadRequestWithSpecificError(c, "provided type was HTTP but no HTTP info was supplied")
				return
			}
			if !strings.HasPrefix(createReq.HTTPInfo.Host, "https://") && !strings.HasPrefix(createReq.HTTPInfo.Host, "http://") {
				helpers.BadRequestWithSpecificError(c, "HTTP host must start with http:// or https://")
				return
			}
			if err := ValidateAllowedCodes(createReq.HTTPInfo.AllowedCodes); err != nil {
				helpers.BadRequestWithSpecificError(c, err.Error())
				return
			}

			target.HTTPInfo = model.TargetHTTPInfo{
				Host:            createReq.HTTPInfo.Host,
				AllowedCodes:    createReq.HTTPInfo.AllowedCodes,
				FollowRedirects: createReq.HTTPInfo.FollowRedirects,
				VerifySSLCert:   createReq.HTTPInfo.VerifySSLCert,
			}
		}

		if createReq.Type == model.PING {
			if createReq.PingInfo == nil {
				helpers.BadRequestWithSpecificError(c, "provided type was ping but no ping info was specified")
				return
			}
			if helpers.IsBlank(createReq.PingInfo.Host) {
				helpers.BadRequestWithSpecificError(c, "ping host host cannot be blank")
				return
			}
			target.PingInfo = model.TargetPingInfo{
				Host: createReq.PingInfo.Host,
			}
		}

		var checkers []model.Checker
		err = db.Find(&checkers, createReq.CheckerIDs).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		if len(checkers) != len(createReq.CheckerIDs) {
			helpers.BadRequestWithSpecificError(c, "at least one of specified checkers hasn't been found")
			return
		}
		target.Checkers = checkers

		err = db.Save(&target).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		SanitizeTarget(&target)
		response := helpers.HTTPResponse{
			Code: http.StatusCreated,
			Data: target,
		}
		response.WriteAsJSON(c)
	}
}

type createTargetSuccessResponse struct {
	Code int          `json:"code" example:"201"`
	Data model.Target `json:"data"`
}
type CreateTargetRequest struct {
	// Name must not be blank
	Name string `json:"name" binding:"required"`
	// Max retries is required
	MaxRetries uint `json:"maxRetries" binding:"requiredUint"`
	// Type must be 0 (PING) or 1 (HTTP)
	Type model.TargetType `json:"type" binding:"requiredUint"`
	// Timeout is required
	Timeout uint `json:"timeout" binding:"requiredUint"`
	// At least one checker must be supplied, all checkers must exist
	CheckerIDs []uint `json:"checkerIDs" binding:"required,min=1"`
	// Must be supplied if type is 0 (PING)
	PingInfo *PingInfoCreateRequest `json:"pingInfo,omitempty"`
	// Must be supplied if type is 1 (HTTP)
	HTTPInfo *HTTPInfoCreateRequest `json:"httpInfo,omitempty"`
}
type HTTPInfoCreateRequest struct {
	// Host must start with http:// or https://
	Host string `json:"host" binding:"required"`
	// HTTP response codes separated by a space. Must contain at least one code. All codes must be exactly 3 digits long.
	AllowedCodes    string `json:"allowedCodes" binding:"required"`
	FollowRedirects bool   `json:"followRedirects" binding:"required"`
	VerifySSLCert   bool   `json:"verifySSLCert" binding:"required"`
}

type PingInfoCreateRequest struct {
	// Host must not be blank
	Host string `json:"host" binding:"required"`
}
