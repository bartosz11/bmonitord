package target

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
	"strings"
)

func HandleCreateTarget(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		createReq := CreateRequest{}

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
			Code: http.StatusOK,
			Data: target,
		}
		response.WriteAsJSON(c)
	}
}

type CreateRequest struct {
	Name       string                 `json:"name" binding:"required"`
	MaxRetries uint                   `json:"maxRetries" binding:"requiredUint"`
	Type       model.TargetType       `json:"type" binding:"requiredUint"`
	Timeout    uint                   `json:"timeout" binding:"requiredUint"`
	CheckerIDs []uint                 `json:"checkerIDs" binding:"required,min=1"`
	PingInfo   *PingInfoCreateRequest `json:"pingInfo,omitempty"`
	HTTPInfo   *HTTPInfoCreateRequest `json:"httpInfo,omitempty"`
}
type HTTPInfoCreateRequest struct {
	Host            string `json:"host" binding:"required"`
	AllowedCodes    string `json:"allowedCodes" binding:"required"`
	FollowRedirects bool   `json:"followRedirects" binding:"required"`
	VerifySSLCert   bool   `json:"verifySSLCert" binding:"required"`
}

type PingInfoCreateRequest struct {
	Host string `json:"host" binding:"required"`
}
