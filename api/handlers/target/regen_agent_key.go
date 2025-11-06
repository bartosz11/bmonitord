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

// HandleAgentKeyReset docs
// @Summary Regenerate target's agent's key
// @Description Allows a user to regenerate the key of agent of target with specified ID
// @Tags target
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param targetID path uint true "ID of target to regenerate agent's key"
// @Success 200 {object} getTargetSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given ID or pause status couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a target with given ID couldn't be found or it's type isn't 2 (agent)."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /target/{targetID}/agent/key [patch]
func HandleAgentKeyReset(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		value, _ := c.Get("user")
		user := value.(model.User)

		param := c.Param("targetID")
		id, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "target id")
			return
		}

		var target model.Target
		err = db.Joins("Agent").First(&target, "targets.user_id = ? and targets.id = ?", user.ID, id).Error

		if errors.Is(err, gorm.ErrRecordNotFound) || target.Type != model.AGENT {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		newKey := GenerateUniqueAgentKey(db)
		if newKey == "" {
			helpers.DBInteractionFailed(c)
			return
		}

		target.Agent.Key = newKey
		if db.Session(&gorm.Session{FullSaveAssociations: true}).Save(&target).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		SanitizeTarget(&target)
		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: target,
		}
		resp.WriteAsJSON(c)
	}
}
