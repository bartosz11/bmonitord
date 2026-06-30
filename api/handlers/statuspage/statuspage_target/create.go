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

// HandleCreateStatuspageTarget docs
// @Summary Create a statuspage target
// @Description Allows a user to create a statuspage target (associate a target with a statuspage, and optionally put it into a group).
// @Tags statuspageTarget
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage"
// @Param createReq body CreateStatuspageTargetRequest true "Information about new statuspage target"
// @Success 201 {object} createStatuspageTargetSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body doesn't match requirements or statuspage ID couldn't be parsed or such association between given entities already exists."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage, target or statuspage group (if provided) with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/target [post]
func HandleCreateStatuspageTarget(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("statuspageId")
		statuspageId, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "statuspage id")
			return
		}

		var createReq CreateStatuspageTargetRequest
		if c.ShouldBind(&createReq) != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
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

		var target model.Target
		err = db.First(&target, "id = ? and user_id = ?", createReq.TargetID, user.ID).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		var count int64
		err = db.Model(&model.StatuspageTarget{}).Where("target_id = ? and statuspage_id = ?", target.ID, statuspage.ID).Count(&count).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		if count != 0 {
			helpers.BadRequestWithSpecificError(c, "given target is already assigned to statuspage")
			return
		}

		pos := float64(0)
		if createReq.Position != nil {
			if *createReq.Position < 0 {
				helpers.BadRequestWithSpecificError(c, "position must be a non-negative number")
				return
			}
			pos = *createReq.Position
		}

		statuspageTarget := model.StatuspageTarget{
			Position:     pos,
			StatuspageID: statuspage.ID,
			TargetID:     target.ID,
		}

		if createReq.GroupID != nil {
			// we also need to check for the group's existence and whether it belongs to that statuspage if there's one provided
			groupId := *createReq.GroupID
			var group model.StatuspageGroup
			err = db.First(&group, "id = ? and statuspage_id = ?", groupId, statuspage.ID).Error
			if errors.Is(err, gorm.ErrRecordNotFound) {
				helpers.NotFound(c)
				return
			}

			if err != nil {
				helpers.DBInteractionFailed(c)
				return
			}

			statuspageTarget.StatuspageGroupID = &group.ID
		}

		err = db.Save(&statuspageTarget).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusCreated,
			Data: statuspageTarget,
		}
		resp.WriteAsJSON(c)
	}
}

type CreateStatuspageTargetRequest struct {
	// TargetID is required and the target with given ID must exist and belong to the same user as the statuspage
	TargetID uint `json:"targetId" binding:"requiredUint"`
	// GroupID is optional, but the group must exist and belong to the statuspage if provided
	GroupID *uint `json:"groupId"`
	// Position is optional, must be a non-negative number if supplied. Value of 0 is persisted if not provided.
	Position *float64 `json:"position"`
}

type createStatuspageTargetSuccessResponse struct {
	Code int                    `json:"code" example:"201"`
	Data model.StatuspageTarget `json:"data"`
}
