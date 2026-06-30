package group

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleUpdateStatuspageGroupById docs
// @Summary Update a statuspage group
// @Description Allows a user to update information about a group belonging to a statuspage with given IDs
// @Tags statuspageGroup
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage the group belongs to"
// @Param groupId path uint true "ID of statuspage group"
// @Param updateReq body UpdateStatuspageGroupRequest true "New information about statuspage group"
// @Success 200 {object} getStatuspageGroupSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when statuspage or group ID couldn't be parsed or when request body doesn't match requirements."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage or group with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/group/{groupId} [patch]
func HandleUpdateStatuspageGroupById(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("statuspageId")
		statuspageId, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "statuspage id")
			return
		}

		param = c.Param("groupId")
		groupId, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "group id")
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

		var group model.StatuspageGroup
		err = db.First(&group, "id = ? and statuspage_id = ?", groupId, statuspageId).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		updateReq := UpdateStatuspageGroupRequest{}
		if c.ShouldBind(&updateReq) != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
			return
		}

		if updateReq.Name != nil {
			if helpers.IsBlank(*updateReq.Name) {
				helpers.BadRequestWithSpecificError(c, "name cannot be blank")
				return
			}
			group.Name = *updateReq.Name
		}

		if updateReq.Description != nil {
			group.Description = *updateReq.Description
		}

		if updateReq.Position != nil {
			if *updateReq.Position < 0 {
				helpers.BadRequestWithSpecificError(c, "position must be a non-negative number")
				return
			}
			group.Position = *updateReq.Position
		}

		if db.Save(&group).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: group,
		}
		resp.WriteAsJSON(c)
	}
}

type UpdateStatuspageGroupRequest struct {
	// Name must not be empty if specified
	Name *string `json:"name"`
	// Description is optional, empty string can be specified to reset
	Description *string `json:"description"`
	// Position is optional, must be a non-negative number if provided
	Position *float64 `json:"position"`
}
