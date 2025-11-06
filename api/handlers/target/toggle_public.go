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

// HandleTargetTogglePublic docs
// @Summary Make target public/private
// @Description Allows a user to make Target and it's incidents and heartbeats public or private
// @Tags target
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param targetID path uint true "ID of target to publish/unpublish"
// @Param public query bool false "Public status, can be true for public, false for private. If not supplied, target's public status will change to the opposite of current status."
// @Success 200 {object} getTargetSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given ID or public status couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a target with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /target/{targetID}/public [patch]
func HandleTargetTogglePublic(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		// This endpoint is almost identical to the pause endpoint, hence the copypasta
		value, _ := c.Get("user")
		user := value.(model.User)

		param := c.Param("targetID")
		id, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "target id")
			return
		}

		var target model.Target
		err = db.First(&target, "user_id = ? and id = ?", user.ID, id).Error

		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		publicParam := c.Query("public")
		if publicParam == "" {
			//The endpoint behaves like a "toggle switch" if value isn't supplied at all (missing or empty param)
			target.Public = !target.Public
		} else {
			//Otherwise behave normally
			public, err := strconv.ParseBool(publicParam)
			if err != nil {
				helpers.ParsingFailed(c, "public status")
				return
			} else {
				target.Public = public
			}
		}

		if db.Save(&target).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		//Just in case
		SanitizeTarget(&target)
		response := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: target,
		}
		response.WriteAsJSON(c)
	}
}
