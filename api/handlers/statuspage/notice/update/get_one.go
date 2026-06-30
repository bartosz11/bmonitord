package update

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleGetStatuspageNoticeUpdateById docs
// @Summary Get a statuspage notice update by IDs
// @Description Allows a user to get an update of a notice on their statuspage with IDs of statuspage, notice and update
// @Tags statuspageNoticeUpdate
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage"
// @Param noticeId path uint true "ID of statuspage notice"
// @Param updateId path uint true "ID of statuspage notice update"
// @Success 200 {object} getStatuspageNoticeUpdateSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when statuspage or notice or update ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a notice update with given IDs couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/notice/{noticeId}/update/{updateId} [get]
func HandleGetStatuspageNoticeUpdateById(db *gorm.DB) gin.HandlerFunc {
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

		param = c.Param("updateId")
		updateId, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "update id")
			return
		}

		err = db.Joins("Statuspage").First(&model.StatuspageNotice{}, "statuspage_notices.id = ? and statuspage_notices.statuspage_id = ? and statuspages.user_id = ?", noticeId, statuspageId, user.ID).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		var update model.StatuspageNoticeUpdate
		err = db.First(&update, "id = ? and notice_id = ?", updateId, noticeId).Error
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
			Data: update,
		}
		resp.WriteAsJSON(c)
	}
}

type getStatuspageNoticeUpdateSuccessResponse struct {
	Code int                          `json:"code" example:"200"`
	Data model.StatuspageNoticeUpdate `json:"data"`
}
