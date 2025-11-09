package incident

import (
	"errors"
	"strconv"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleGetLastIncidentForTargetId docs
// @Summary Get last incident of target
// @Description Allows to retrieve information about the last incident of target with specified ID, if target is public then the info can be retrieved by anyone, even without an auth token
// @Tags incident
// @Accept json
// @Produce json
// @Param id path uint true "ID of target to get the last incident info of"
// @Success 200 {object} getIncidentSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given ID couldn't be parsed."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a target with given ID couldn't be found or user sending the request isn't allowed to access it (target isn't public)."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /incident/{id}/last [get]
func HandleGetLastIncidentForTargetId(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		param := c.Param("id")
		targetID, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "target id")
			return
		}

		val, userAuthenticated := c.Get("user")
		var user model.User
		if userAuthenticated {
			user = val.(model.User)
		}

		var incident model.Incident
		// last incident may not have an end date so we sort by start date instead
		err = db.Joins("Target").Order("incidents.start desc").First(&incident, `"Target"."id" = ? and ("Target"."public" = true or ("Target"."user_id" = ? and ?))`, targetID, user.ID, userAuthenticated).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: 200,
			Data: incident,
		}
		resp.WriteAsJSON(c)
	}
}
