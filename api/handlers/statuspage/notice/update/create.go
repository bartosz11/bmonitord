package update

import (
	"errors"
	"net/http"
	"strconv"
	"time"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleCreateStatuspageNoticeUpdate docs
// @Summary Create a statuspage notice update
// @Description Allows a user to create an update for a notice on a statuspage.
// @Tags statuspageNoticeUpdate
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage"
// @Param noticeId path uint true "ID of statuspage notice"
// @Param createReq body CreateStatuspageNoticeUpdateRequest true "Information about new statuspage notice update"
// @Success 201 {object} createStatuspageNoticeUpdateSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body doesn't match requirements or statuspage ID or notice ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage notice with given IDs couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/notice/{noticeId}/update [post]
func HandleCreateStatuspageNoticeUpdate(db *gorm.DB) gin.HandlerFunc {
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

		var notice model.StatuspageNotice
		err = db.Joins("Statuspage").First(&notice, "statuspage_notices.id = ? and statuspage_notices.statuspage_id = ? and statuspage.user_id = ?", noticeId, statuspageId, user.ID).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		var createReq CreateStatuspageNoticeUpdateRequest
		if c.ShouldBind(&createReq) != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
			return
		}

		if err = model.ValidateStatuspageNoticeStatus(createReq.Status); err != nil {
			helpers.BadRequestWithSpecificError(c, err.Error())
			return
		}

		if helpers.IsBlank(createReq.Message) {
			helpers.BadRequestWithSpecificError(c, "message must not be empty")
			return
		}

		update := model.StatuspageNoticeUpdate{
			Message:  createReq.Message,
			Status:   createReq.Status,
			Date:     time.Now(),
			NoticeID: notice.ID,
		}

		if db.Save(&update).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		notice.Status = createReq.Status
		if db.Save(&notice).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusCreated,
			Data: update,
		}
		resp.WriteAsJSON(c)
	}
}

type CreateStatuspageNoticeUpdateRequest struct {
	// Status is required, the notice's status will be changed to this value
	Status model.StatuspageNoticeStatus `json:"status" binding:"requiredUint"`
	// Message must not be empty
	Message string `json:"message" binding:"required"`
}

type createStatuspageNoticeUpdateSuccessResponse struct {
	Code int                          `json:"code" example:"201"`
	Data model.StatuspageNoticeUpdate `json:"data"`
}
