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

// HandleGetAllStatuspageNoticeUpdates docs
// @Summary Get all updates of a statuspage notice
// @Description Allows a user to get a list of updates for a notice on their statuspage
// @Tags statuspageNoticeUpdate
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage"
// @Param noticeId path uint true "ID of notice to list updates for"
// @Success 200 {object} listStatuspageNoticeUpdatesSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when statuspage or notice ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage notice with given IDs couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/notice/{noticeId}/update [get]
func HandleGetAllStatuspageNoticeUpdates(db *gorm.DB) gin.HandlerFunc {
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

		err = db.Joins("Statuspage").First(&model.StatuspageNotice{}, "statuspage_notices.id = ? and statuspage_notices.statuspage_id = ? and statuspages.user_id = ?", noticeId, statuspageId, user.ID).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		var updates []model.StatuspageNoticeUpdate
		if db.Find(&updates, "notice_id = ?", noticeId).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: updates,
		}
		resp.WriteAsJSON(c)
	}
}

type listStatuspageNoticeUpdatesSuccessResponse struct {
	Code int                            `json:"code" example:"200"`
	Data []model.StatuspageNoticeUpdate `json:"data"`
}
