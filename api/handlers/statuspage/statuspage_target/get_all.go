package statuspage_target

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleGetAllStatuspageTargets docs
// @Summary Get targets linked to statuspage
// @Description Allows a user to retrieve a list of statuspage targets.
// @Tags statuspageTarget
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage"
// @Success 200 {object} listStatuspageTargetsSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when statuspage ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/target [get]
func HandleGetAllStatuspageTargets(db *gorm.DB) gin.HandlerFunc {
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

		var targets []model.StatuspageTarget
		err = db.Find(&targets, "statuspage_id = ?", statuspageId).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: targets,
		}
		resp.WriteAsJSON(c)
	}
}

type listStatuspageTargetsSuccessResponse struct {
	Code int                      `json:"code" example:"200"`
	Data []model.StatuspageTarget `json:"data"`
}
