package notice

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

// HandleUpdateStatuspageNoticeById docs
// @Summary Update a statuspage notice
// @Description Allows a user to update information about a notice with specified ID that belong to a statuspage with given ID.
// @Tags statuspageNotice
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage"
// @Param noticeId path uint true "ID of statuspage notice update"
// @Param updateReq body UpdateStatuspageNoticeRequest true "New information about statuspage notice"
// @Success 200 {object} getStatuspageNoticeSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body doesn't match requirements or statuspage or notice ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage notice with given IDs couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/notice/{noticeId} [patch]
func HandleUpdateStatuspageNoticeById(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		// This update endpoint is implemented in a different way than all the others, because apparently I haven't thought of this way earlier
		// I know that as of now it's very, very duplicate when compared to the create endpoint, I'll get around refactoring the API at some point
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

		var updateReq UpdateStatuspageNoticeRequest
		if c.ShouldBind(&updateReq) != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
			return
		}
		// check ownership - if user owns the statuspage it owns all things which belong to that statuspage
		err = db.First(&model.Statuspage{}, "id = ? and user_id = ?", statuspageId, user.ID).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}
		//fetch the entity
		var notice model.StatuspageNotice
		err = db.First(&notice, "id = ? and statuspage_id = ?", noticeId, statuspageId).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}
		// apply the update
		if updateReq.Title != nil {
			notice.Title = *updateReq.Title
		}

		if updateReq.Severity != nil {
			notice.Severity = *updateReq.Severity
		}

		if updateReq.StartedAt != nil {
			notice.StartedAt = updateReq.StartedAt
		}

		if updateReq.ResolvedAt != nil {
			notice.ResolvedAt = updateReq.ResolvedAt
		}

		if updateReq.ScheduledStartAt != nil {
			notice.ScheduledStartAt = updateReq.ScheduledStartAt
		}

		if updateReq.ScheduledEndAt != nil {
			notice.ScheduledEndAt = updateReq.ScheduledEndAt
		}

		if updateReq.TargetIDs != nil {
			// had to borrow this from create for now, there's no better way than just validating it in place, although I'll extract it later
			idsLength := len(*updateReq.TargetIDs)
			if idsLength != 0 {
				var targets []model.Target
				err = db.Joins("StatuspageTarget").Find(&targets, "statuspage_id = ? and target_id in ?", statuspageId, updateReq.TargetIDs).Error
				if err != nil {
					helpers.DBInteractionFailed(c)
					return
				}

				if len(targets) != idsLength {
					helpers.BadRequestWithSpecificError(c, "at least one of the specified targets doesn't belong to the specified statuspage")
					return
				}

				notice.Targets = targets
			} else {
				notice.Targets = []model.Target{}
			}
		}
		// validate
		if helpers.IsBlank(notice.Title) {
			helpers.BadRequestWithSpecificError(c, "title cannot be empty")
			return
		}

		err = model.ValidateStatuspageNoticeType(notice.Type)
		if err != nil {
			helpers.BadRequestWithSpecificError(c, err.Error())
			return
		}

		err = model.ValidateStatuspageNoticeSeverity(notice.Severity)
		if err != nil {
			helpers.BadRequestWithSpecificError(c, err.Error())
			return
		}

		if notice.Severity != model.None && notice.Type == model.Announcement {
			helpers.BadRequestWithSpecificError(c, "notices of type \"announcement\" must use severity \"none\"")
			return
		}

		if notice.StartedAt != nil && notice.ResolvedAt != nil && !notice.StartedAt.Before(*notice.ResolvedAt) {
			helpers.BadRequestWithSpecificError(c, "StartedAt must be before ResolvedAt")
			return
		}

		if (notice.ScheduledStartAt == nil && notice.ScheduledEndAt != nil) || (notice.ScheduledEndAt == nil && notice.ScheduledStartAt != nil) {
			helpers.BadRequestWithSpecificError(c, "ScheduledStartAt and ScheduledEndAt must be both set or both be omitted after update")
			return
		}

		if notice.ScheduledStartAt != nil && notice.ScheduledEndAt != nil && !notice.ScheduledStartAt.Before(*notice.ScheduledEndAt) {
			helpers.BadRequestWithSpecificError(c, "ScheduledStartAt must be before ScheduledEndAt")
			return
		}
		// since everything's correct now, we can persist the changes
		if db.Session(&gorm.Session{FullSaveAssociations: true}).Save(&notice).Error != nil {
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

type UpdateStatuspageNoticeRequest struct {
	// Title is optional, must not be empty if provided
	Title *string `json:"title"`
	// Severity is optional. Notices of type announcement (2) must use severity "none" (0)
	Severity *model.StatuspageNoticeSeverity `json:"severity"`
	// StartedAt is optional, must be before ResolvedAt if its value isn't empty in the request or in the notice
	StartedAt *time.Time `json:"startedAt"`
	// ResolvedAt is optional, must be after StartedAt if its value isn't empty in the request or in the notice
	ResolvedAt *time.Time `json:"resolvedAt"`
	// ScheduledStartAt is optional, if supplied ScheduledEndAt must be also supplied or must be already set for the notice. ScheduledStartAt must a timestamp before ScheduledEndAt.
	ScheduledStartAt *time.Time `json:"scheduledStartAt"`
	// ScheduledEndAt is optional, if supplied ScheduledStartAt must be also supplied or must be already set for the notice. ScheduledEndAt must a timestamp after ScheduledStartAt.
	ScheduledEndAt *time.Time `json:"scheduledEndAt"`
	// TargetIDs are optional. If provided, they replace the current target list. All specified targets must belong to the same statuspage as the notice.
	TargetIDs *[]uint `json:"targets"`
}
