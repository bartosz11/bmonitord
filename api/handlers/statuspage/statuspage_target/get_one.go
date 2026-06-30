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

// HandleGetStatuspageTargetByIds docs
// @Summary Get a statuspage target
// @Description Allows a user to retrieve a statuspage target with the statuspage and target ID.
// @Tags statuspageTarget
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage"
// @Param targetId path uint true "ID of target"
// @Success 200 {object} getStatuspageTargetSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when statuspage or target ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage or statuspage target with given IDs couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/target/{targetId} [get]
func HandleGetStatuspageTargetByIds(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("statuspageId")
		statuspageId, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "statuspage id")
			return
		}

		param = c.Param("targetId")
		targetId, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "target id")
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
		// We don't need to check for ownership of target by the user since it has to be owned by the same user to create a StatuspageTarget
		var statuspageTarget model.StatuspageTarget
		err = db.First(&statuspageTarget, "target_id = ? and statuspage_id = ?", targetId, statuspageId).Error
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
			Data: statuspageTarget,
		}
		resp.WriteAsJSON(c)
	}
}

type getStatuspageTargetSuccessResponse struct {
	Code int                    `json:"code" example:"200"`
	Data model.StatuspageTarget `json:"data"`
}
