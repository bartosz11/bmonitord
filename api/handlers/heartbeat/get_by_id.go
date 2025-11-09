package heartbeat

import (
	"errors"
	"strconv"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleGetHeartbeatById docs
// @Summary Get heartbeat by ID
// @Description Allows to retrieve information about heartbeat with specified ID, if heartbeat's target is public then the info can be retrieved by anyone, even without an auth token
// @Tags heartbeat
// @Accept json
// @Produce json
// @Param id path uint true "ID of heartbeat to get"
// @Success 200 {object} getHeartbeatSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given ID couldn't be parsed."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a heartbeat with given ID couldn't be found or user sending the request isn't allowed to access it (target isn't public)."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /heartbeat/{id} [get]
func HandleGetHeartbeatById(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		param := c.Param("id")
		id, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "heartbeat id")
			return
		}

		val, userAuthenticated := c.Get("user")
		var user model.User
		if userAuthenticated {
			user = val.(model.User)
		}

		var heartbeat model.Heartbeat
		err = db.Joins("Target").First(&heartbeat, `heartbeats.id = ? and ("Target"."public" = true or ("Target"."user_id" = ? and ?))`, id, user.ID, userAuthenticated).Error
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
			Data: heartbeat,
		}
		resp.WriteAsJSON(c)
	}
}

type getHeartbeatSuccessResponse struct {
	Code int             `json:"code" example:"200"`
	Data model.Heartbeat `json:"data"`
}
