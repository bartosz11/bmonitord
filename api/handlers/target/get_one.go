package target

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleGetTargetByID docs
// @Summary Get target by ID
// @Description Allows to retrieve information about target with specified ID, if target is public then the info can be retrieved by anyone, even without an auth token
// @Tags target
// @Accept json
// @Produce json
// @Param targetID path uint true "ID of target to get"
// @Success 200 {object} getTargetSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given ID couldn't be parsed."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a target with given ID couldn't be found or user sending the request isn't allowed to access it (target isn't public)."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /target/{targetID} [get]
func HandleGetTargetByID(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		param := c.Param("targetID")
		id, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "target id")
			return
		}

		val, authenticatedUser := c.Get("user")
		var user model.User
		if authenticatedUser {
			user = val.(model.User)
		}

		var target model.Target
		err = db.Joins("HTTPInfo").Joins("PingInfo").Joins("Agent").Preload("Checkers").Preload("Alarms").
			First(&target, "targets.id = ?", id).Error

		// !authenticatedUser prevents a case where targets of a user with id 0 (shouldn't exist) can be retrieved by anyone, because user.ID default value is 0
		if errors.Is(err, gorm.ErrRecordNotFound) || (!target.Public && (!authenticatedUser || user.ID != target.UserID)) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		SanitizeTarget(&target)
		if !authenticatedUser || user.ID != target.UserID {
			target.Agent.Key = ""
		}
		response := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: target,
		}
		response.WriteAsJSON(c)
	}
}

type getTargetSuccessResponse struct {
	Code int          `json:"code" example:"200"`
	Data model.Target `json:"data"`
}
