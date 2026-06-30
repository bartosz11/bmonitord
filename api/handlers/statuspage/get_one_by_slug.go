package statuspage

import (
	"errors"
	"net/http"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleGetStatuspageBySlug docs
// @Summary Get statuspage by slug
// @Description Allows a user retrieve information about statuspage with specified slug.
// @Tags statuspage
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param slug path string true "Slug of statuspage to get"
// @Success 200 {object} getStatuspageSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given slug is blank."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage with given slug couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/slug/{slug} [get]
func HandleGetStatuspageBySlug(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		slug := c.Param("slug")
		if helpers.IsBlank(slug) {
			helpers.BadRequestWithSpecificError(c, "slug must not be blank")
			return
		}

		var statuspage model.Statuspage
		err := db.First(&statuspage, "slug = ? and user_id = ?", slug, user.ID).Error

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
			Data: statuspage,
		}
		resp.WriteAsJSON(c)
	}
}
