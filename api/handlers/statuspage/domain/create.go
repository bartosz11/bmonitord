package domain

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleCreateStatuspageDomain docs
// @Summary Create a statuspage domain
// @Description Allows a user to create a domain for a statuspage.
// @Tags statuspageDomain
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage to assign the new domain to"
// @Param createReq body CreateStatuspageDomainRequest true "Information about new statuspage domain"
// @Success 201 {object} createStatuspageDomainSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body doesn't match requirements or statuspage ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/domain [post]
func HandleCreateStatuspageDomain(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("statuspageId")
		statuspageId, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "statuspage id")
			return
		}

		var statuspage model.Statuspage
		err = db.First(&statuspage, "id = ? and user_id = ?", statuspageId, user.ID).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		createReq := CreateStatuspageDomainRequest{}
		if c.ShouldBind(&createReq) != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
			return
		}

		if helpers.IsBlank(createReq.Domain) {
			helpers.BadRequestWithSpecificError(c, "domain cannot be empty")
			return
		}

		var count int64
		err = db.Model(&model.StatuspageDomain{}).Where("domain = ?", createReq.Domain).Count(&count).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		if count != 0 {
			helpers.BadRequestWithSpecificError(c, "domain is already taken")
			return
		}

		domain := model.StatuspageDomain{
			Domain:       createReq.Domain,
			StatuspageID: statuspage.ID,
		}

		if db.Save(&domain).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusCreated,
			Data: domain,
		}
		resp.WriteAsJSON(c)
	}
}

type CreateStatuspageDomainRequest struct {
	// Domain must not be blank and must not be already taken across the instance
	Domain string `json:"domain" binding:"required"`
}

type createStatuspageDomainSuccessResponse struct {
	Code int                    `json:"code" example:"201"`
	Data model.StatuspageDomain `json:"data"`
}
