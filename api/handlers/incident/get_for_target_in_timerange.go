package incident

import (
	"net/http"
	"strconv"
	"time"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleGetIncidentsForTargetInTimeRange docs
// @Summary Get incidents of target in time range
// @Description Allows to retrieve a list of incidents of a target that have started in the specified time range, if target is public then the info can be retrieved by anyone, even without an auth token. This endpoint may return an empty list if target is not public. The list is ordered by incident's start timestamp ascending (oldest first).
// @Tags incident
// @Accept json
// @Produce json
// @Param id path uint true "ID of target to get the list of incidents for"
// @Param start query uint true "Unix epoch second representing start of the time range"
// @Param end query uint false "Unix epoch second representing end of the time range. If not specified, current time is used as end timestamp of the time range."
// @Success 200 {object} getManyIncidentsSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when a parameter couldn't be parsed."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /incident/{id}/timerange [get]
func HandleGetIncidentsForTargetInTimeRange(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		startParam := c.Query("start")
		if startParam == "" {
			helpers.BadRequestWithSpecificError(c, "missing start timestamp")
			return
		}
		startInt, err := strconv.ParseInt(startParam, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "start timestamp")
			return
		}
		start := time.Unix(startInt, 0)

		end := time.Now()
		endParam := c.Query("end")
		if endParam != "" {
			endInt, err := strconv.ParseInt(startParam, 10, 64)
			if err != nil {
				helpers.ParsingFailed(c, "end timestamp")
				return
			}
			end = time.Unix(endInt, 0)
		}

		idParam := c.Param("id")
		targetId, err := strconv.ParseUint(idParam, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "target id")
			return
		}

		val, authenticatedUser := c.Get("user")
		var user model.User
		if authenticatedUser {
			user = val.(model.User)
		}

		var incidents []model.Incident
		err = db.Joins("Target").Order("incidents.start asc").Find(&incidents, `"Target"."id" = ? and incidents.start between ? and ? and ("Target"."public" = true or ("Target"."user_id" = ? and ?))`, targetId, start, end, user.ID, authenticatedUser).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: incidents,
		}
		resp.WriteAsJSON(c)
	}
}

type getManyIncidentsSuccessResponse struct {
	Code int              `json:"code" example:"200"`
	Data []model.Incident `json:"data"`
}
