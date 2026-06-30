package notice

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleGetAllStatuspageNotices docs
// @Summary Get all notices for a statuspage
// @Description Allows a user to get a list of notices on their statuspage
// @Tags statuspageNotice
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage to list notices for"
// @Success 200 {object} listStatuspageNoticesSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when statuspage ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/notice [get]
func HandleGetAllStatuspageNotices(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("statuspageId")
		statuspageId, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "statuspage id")
			return
		}

		err = db.First(&model.Statuspage{}, "id = ? and user_id = ?", statuspageId, user.ID).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		var notices []model.StatuspageNotice
		err = db.Joins("Updates").Find(&notices, "statuspage_id = ?", statuspageId).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: notices,
		}
		resp.WriteAsJSON(c)
	}
}

type listStatuspageNoticesSuccessResponse struct {
	Code int                      `json:"code" example:"200"`
	Data []model.StatuspageNotice `json:"data"`
}
