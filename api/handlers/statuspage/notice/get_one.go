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

// HandleGetStatuspageNoticeById docs
// @Summary Get a statuspage notice by ID
// @Description Allows a user to retrieve a notice with specified ID that belongs to a statuspage with given ID.
// @Tags statuspageNotice
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage"
// @Param noticeId path uint true "ID of statuspage notice"
// @Success 200 {object} getStatuspageNoticeSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when statuspage or notice ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage or notice with given IDs couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/notice/{noticeId} [get]
func HandleGetStatuspageNoticeById(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("statuspageId")
		statuspageId, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "statuspage id")
			return
		}

		param = c.Param("noticeId")
		noticeId, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "notice id")
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

		var notice model.StatuspageNotice
		err = db.Joins("Updates").First(&notice, "id = ? and statuspage_id = ?", noticeId, statuspageId).Error
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
			Data: notice,
		}
		resp.WriteAsJSON(c)
	}
}

type getStatuspageNoticeSuccessResponse struct {
	Code int                    `json:"code" example:"200"`
	Data model.StatuspageNotice `json:"data"`
}
