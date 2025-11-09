package incident

import (
	"errors"
	"strconv"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleGetIncidentById docs
// @Summary Get incident by ID
// @Description Allows to retrieve information about incident with specified ID, if incident's target is public then the info can be retrieved by anyone, even without an auth token
// @Tags incident
// @Accept json
// @Produce json
// @Param id path uint true "ID of incident to get"
// @Success 200 {object} getIncidentSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given ID couldn't be parsed."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when an incident with given ID couldn't be found or user sending the request isn't allowed to access it (target isn't public)."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /incident/{id} [get]
func HandleGetIncidentById(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		param := c.Param("id")
		id, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "incident id")
			return
		}

		val, userAuthenticated := c.Get("user")
		var user model.User
		if userAuthenticated {
			user = val.(model.User)
		}

		var incident model.Incident
		// this is similar to the condition we check in HandleGetTargetByID but smuggled into the SQL query
		err = db.Joins("Target").First(&incident, `incidents.id = ? and ("Target"."public" = true or ("Target"."user_id" = ? and ?))`, id, user.ID, userAuthenticated).Error
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

type getIncidentSuccessResponse struct {
	Code int            `json:"code" example:"200"`
	Data model.Incident `json:"data"`
}
