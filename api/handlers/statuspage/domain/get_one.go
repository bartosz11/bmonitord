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

// HandleGetStatuspageDomainById docs
// @Summary Get a statuspage domain by ID
// @Description Allows a user to retrieve information about a domain with specified ID that belongs to a statuspage with given ID.
// @Tags statuspageDomain
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage that owns the domain"
// @Param domainId path uint true "ID of statuspage domain"
// @Success 200 {object} getStatuspageDomainSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when statuspage or domain ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage domain with given IDs couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/domain/{domainId} [get]
func HandleGetStatuspageDomainById(db *gorm.DB) gin.HandlerFunc {
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

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: domain,
		}
		resp.WriteAsJSON(c)
	}
}

type getStatuspageDomainSuccessResponse struct {
	Code int                    `json:"code" example:"200"`
	Data model.StatuspageDomain `json:"data"`
}
