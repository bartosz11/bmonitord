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

// HandleUpdateStatuspageDomainById docs
// @Summary Update a statuspage domain
// @Description Allows a user to update information about a statuspage domain with specified ID owned by a statuspage with given ID.
// @Tags statuspageDomain
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage that owns the domain"
// @Param domainId path uint true "ID of statuspage domain to update"
// @Param updateReq body UpdateStatuspageDomainRequest true "New information about statuspage domain"
// @Success 200 {object} getStatuspageDomainSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body doesn't match requirements or statuspage or domain ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage domain with given IDs couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/domain/{domainId} [patch]
func HandleUpdateStatuspageDomainById(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("statuspageId")
		statuspageId, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "statuspage id")
			return
		}

		param = c.Param("domainId")
		domainId, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "domain id")
			return
		}

		var domain model.StatuspageDomain
		err = db.Joins("Statuspage").First(&domain, "statuspage_domains.id = ? and statuspage_id = ? and statuspages.user_id = ?", domainId, statuspageId, user.ID).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		var updateReq UpdateStatuspageDomainRequest
		if c.ShouldBind(&updateReq) != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
			return
		}

		newDomain := updateReq.Domain
		if newDomain != nil {
			if helpers.IsBlank(*newDomain) {
				helpers.BadRequestWithSpecificError(c, "domain cannot be empty")
				return
			}

			var count int64
			err = db.Model(&model.StatuspageDomain{}).Where("domain = ?", *newDomain).Count(&count).Error
			if err != nil {
				helpers.DBInteractionFailed(c)
				return
			}

			if count != 0 {
				helpers.BadRequestWithSpecificError(c, "domain is already taken")
				return
			}

			domain.Domain = *newDomain
		}

		if db.Save(&domain).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: domain,
		}
		resp.WriteAsJSON(c)
	}

}

type UpdateStatuspageDomainRequest struct {
	// Domain cannot be blank or taken across the instance if supplied
	Domain *string `json:"domain"`
}
