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

// HandleDeleteStatuspageGroupById docs
// @Summary Delete a statuspage group
// @Description Allows a user to delete a group belonging to a statuspage with given IDs
// @Tags statuspageGroup
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage the group belongs to"
// @Param groupId path uint true "ID of statuspage group"
// @Success 204 {object} helpers.GenericDeleteSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when statuspage or group ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage or group with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/group/{groupId} [delete]
func HandleDeleteStatuspageGroupById(db *gorm.DB) gin.HandlerFunc {
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
		//ungroup all targets on group deletion
		if db.Model(&model.StatuspageTarget{}).Where("statuspage_group_id = ?", groupId).Update("statuspage_group_id", nil).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		if db.Unscoped().Delete(&group).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusNoContent,
		}
		resp.WriteAsJSON(c)
	}
}
