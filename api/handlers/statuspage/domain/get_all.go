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

// HandleGetAllStatuspageDomains docs
// @Summary Get all domains of a statuspage
// @Description Allows a user to get a list of their statuspages domains
// @Tags statuspageDomain
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage to list domains of"
// @Success 200 {object} listStatuspageDomainsSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when statuspage ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/domain [get]
func HandleGetAllStatuspageDomains(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("statuspageId")
		statuspageId, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "statuspage id")
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

		var domains []model.StatuspageDomain
		if db.Find(&domains, "statuspage_id = ?", statuspageId).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: domains,
		}
		resp.WriteAsJSON(c)
	}
}

type listStatuspageDomainsSuccessResponse struct {
	Code int                      `json:"code" example:"200"`
	Data []model.StatuspageDomain `json:"data"`
}
