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
// @Description Allows a user to retrieve information about target with specified ID
// @Tags target
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param targetID path uint true "ID of target to get"
// @Success 200 {object} getTargetSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a target with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /target/{targetID} [get]
func HandleGetTargetByID(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("targetID")
		id, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "target id")
			return
		}

		var target model.Target
		err = db.Joins("HTTPInfo").Joins("PingInfo").Preload("Checkers").Preload("Alarms").
			First(&target, "targets.id = ? and user_id = ?", id, user.ID).Error

		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		SanitizeTarget(&target)
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
