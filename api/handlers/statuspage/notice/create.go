package notice

import (
	"errors"
	"net/http"
	"strconv"
	"time"

	"github.com/bartosz11/checkmate/api/handlers/statuspage/notice/update"
	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleCreateStatuspageNotice docs
// @Summary Create a statuspage notice
// @Description Allows a user to create a notice on a statuspage.
// @Tags statuspageNotice
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage to create the notice in"
// @Param createReq body CreateStatuspageNoticeRequest true "Information about new statuspage notice"
// @Success 201 {object} createStatuspageNoticeSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body doesn't match requirements or statuspage ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/notice [post]
func HandleCreateStatuspageNotice(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("statuspageId")
		statuspageId, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "statuspage id")
			return
		}

		var statuspage model.Statuspage
		err = db.First(&statuspage, "id = ? and user_id", statuspageId, user.ID).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		var createReq CreateStatuspageNoticeRequest
		if c.ShouldBind(&createReq) != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
			return
		}

		if helpers.IsBlank(createReq.Title) {
			helpers.BadRequestWithSpecificError(c, "title cannot be empty")
			return
		}

		err = model.ValidateStatuspageNoticeType(createReq.Type)
		if err != nil {
			helpers.BadRequestWithSpecificError(c, err.Error())
			return
		}

		err = model.ValidateStatuspageNoticeSeverity(createReq.Severity)
		if err != nil {
			helpers.BadRequestWithSpecificError(c, err.Error())
			return
		}

		if createReq.Severity != model.None && createReq.Type == model.Announcement {
			helpers.BadRequestWithSpecificError(c, "notices of type \"announcement\" must use severity \"none\"")
			return
		}

		if (createReq.ScheduledStartAt == nil && createReq.ScheduledEndAt != nil) || (createReq.ScheduledEndAt == nil && createReq.ScheduledStartAt != nil) {
			helpers.BadRequestWithSpecificError(c, "ScheduledStartAt and ScheduledEndAt must be both provided or both be omitted")
			return
		}

		if createReq.ScheduledStartAt != nil && createReq.ScheduledEndAt != nil && !createReq.ScheduledStartAt.Before(*createReq.ScheduledEndAt) {
			helpers.BadRequestWithSpecificError(c, "ScheduledStartAt must be before ScheduledEndAt")
			return
		}

		err = model.ValidateStatuspageNoticeStatus(createReq.FirstUpdate.Status)
		if err != nil {
			helpers.BadRequestWithSpecificError(c, err.Error())
			return
		}

		if helpers.IsBlank(createReq.FirstUpdate.Message) {
			helpers.BadRequestWithSpecificError(c, "first updates message cannot be empty")
			return
		}

		notice := model.StatuspageNotice{
			Title:            createReq.Title,
			StartedAt:        createReq.StartedAt,
			ScheduledStartAt: createReq.ScheduledStartAt,
			ScheduledEndAt:   createReq.ScheduledEndAt,
			Type:             createReq.Type,
			Status:           createReq.FirstUpdate.Status, // Nothing wrong with deriving it here already I guess
			Severity:         createReq.Severity,
			StatuspageID:     uint(statuspageId),
			Updates: []model.StatuspageNoticeUpdate{
				{
					Message: createReq.FirstUpdate.Message,
					Status:  createReq.FirstUpdate.Status,
					Date:    time.Now(),
				},
			},
		}

		idsLength := len(createReq.TargetIDs)
		if idsLength != 0 {
			var targets []model.Target
			err = db.Joins("StatuspageTarget").Find(&targets, "statuspage_id = ? and target_id in ?", statuspageId, createReq.TargetIDs).Error
			if err != nil {
				helpers.DBInteractionFailed(c)
				return
			}

			if len(targets) != idsLength {
				helpers.BadRequestWithSpecificError(c, "at least one of the specified targets doesn't belong to the specified statuspage")
				return
			}

			notice.Targets = targets
		}

		if db.Session(&gorm.Session{FullSaveAssociations: true}).Save(&notice).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusCreated,
			Data: notice,
		}
		resp.WriteAsJSON(c)
	}
}

type CreateStatuspageNoticeRequest struct {
	// Title is required and must not be empty
	Title string `json:"title" binding:"required"`
	// Type is required and cannot be edited afterwards
	Type model.StatuspageNoticeType `json:"type" binding:"requiredUint"`
	// Severity is required, must be set to none (0) for notices of type announcement (2)
	Severity model.StatuspageNoticeSeverity `json:"severity" binding:"requiredUint"`
	// FirstUpdate is required
	FirstUpdate update.CreateStatuspageNoticeUpdateRequest `json:"firstUpdate" binding:"required"`
	// StartedAt is optional
	StartedAt *time.Time `json:"startedAt"`
	// ScheduledStartAt is optional, if supplied ScheduledEndAt must be also supplied and must be after ScheduledStartAt
	ScheduledStartAt *time.Time `json:"scheduledStartAt"`
	// ScheduledEndAt is optional, if supplied ScheduledStartAt must be also supplied and must be before ScheduledStartAt
	ScheduledEndAt *time.Time `json:"scheduledEndAt"`
	// TargetIDs are optional, although if supplied, the targets must all belong to the same statuspage
	TargetIDs []uint `json:"targets"`
}

type createStatuspageNoticeSuccessResponse struct {
	Code int                    `json:"code" example:"201"`
	Data model.StatuspageNotice `json:"data"`
}
