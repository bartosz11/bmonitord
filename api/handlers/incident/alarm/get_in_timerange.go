package alarm

import (
	"net/http"
	"strconv"
	"time"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleGetIncidentsForAlarmInTimeRange docs
// @Summary Get incidents of alarm in time range
// @Description Allows to retrieve a list of incidents of an alarm that have started in the specified time range, if alarm's target is public then the info can be retrieved by anyone, even without an auth token. This endpoint may return an empty list if alarm's target is not public. The list is ordered by incident's start timestamp ascending (oldest first).
// @Tags incident
// @Accept json
// @Produce json
// @Param id path uint true "ID of alarm to get the list of incidents for"
// @Param start query uint true "Unix epoch second representing start of the time range"
// @Param end query uint false "Unix epoch second representing end of the time range. If not specified, current time is used as end timestamp of the time range."
// @Success 200 {object} target.GetManyIncidentsSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when a parameter couldn't be parsed."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /incident/alarm/{id}/timerange [get]
func HandleGetIncidentsForAlarmInTimeRange(db *gorm.DB) gin.HandlerFunc {
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
		alarmId, err := strconv.ParseUint(idParam, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "alarm id")
			return
		}

		val, authenticatedUser := c.Get("user")
		var user model.User
		if authenticatedUser {
			user = val.(model.User)
		}

		var incidents []model.Incident
		err = db.Joins("Target").Joins("Alarm").Order("incidents.start asc").Find(&incidents, `"Alarm"."id" = ? and incidents.start between ? and ? and ("Target"."public" = true or ("Target"."user_id" = ? and ?))`, alarmId, start, end, user.ID, authenticatedUser).Error
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
