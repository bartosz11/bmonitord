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

// HandleUpdateStatuspageTargetByIds docs
// @Summary Update a statuspage target
// @Description Allows a user to update information about the placement of a target on a statuspage.
// @Tags statuspageGroup
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage"
// @Param targetId path uint true "ID of target"
// @Param groupId path uint true "ID of statuspage group"
// @Param updateReq body CreateStatuspageTargetRequest true "New information about statuspage target"
// @Success 200 {object} getStatuspageTargetSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when statuspage or target ID couldn't be parsed or when request body doesn't match requirements."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage or statuspage target with given IDs couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/target/{targetId} [patch]
func HandleUpdateStatuspageTargetByIds(db *gorm.DB) gin.HandlerFunc {
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

		var updateReq UpdateStatuspageTargetRequest
		if c.ShouldBind(&updateReq) != nil {
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

		var statuspageTarget model.StatuspageTarget
		err = db.First(&statuspageTarget, "statuspage_id = ? and target_id = ?", statuspageId, targetId).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		if updateReq.Position != nil {
			if *updateReq.Position < 0 {
				helpers.BadRequestWithSpecificError(c, "position must be a non-negative number")
				return
			}
			statuspageTarget.Position = *updateReq.Position
		}

		if updateReq.Ungroup != nil && *updateReq.Ungroup {
			if updateReq.GroupId != nil {
				helpers.BadRequestWithSpecificError(c, "group id cannot be set when ungroup is true")
				return
			}
			statuspageTarget.StatuspageGroupID = nil
		}

		if updateReq.GroupId != nil {
			var group model.StatuspageGroup
			err := db.First(&group, "id = ? and statuspage_id = ?", *updateReq.GroupId, statuspageId).Error
			if errors.Is(err, gorm.ErrRecordNotFound) {
				helpers.NotFound(c)
				return
			}

			if err != nil {
				helpers.DBInteractionFailed(c)
				return
			}
			statuspageTarget.StatuspageGroupID = updateReq.GroupId
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: statuspageTarget,
		}
		resp.WriteAsJSON(c)
	}
}

type UpdateStatuspageTargetRequest struct {
	// Position is optional, must be a non-negative number if supplied.
	Position *float64 `json:"position"`
	// GroupId is an optional field, the group must exist and belong to the same statuspage as the target if supplied. GroupId cannot be supplied if Ungroup is set to true
	GroupId *uint `json:"groupId"`
	// Ungroup is an optional field. If true is given, GroupId will be set to null, effectively removing the target from the group it's currently in. Value of true cannot be supplied along with a GroupId
	Ungroup *bool `json:"ungroup"`
}
