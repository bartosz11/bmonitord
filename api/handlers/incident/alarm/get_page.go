package alarm

import (
	"net/http"
	"strconv"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleGetIncidentPageForAlarm docs
// @Summary Get page of incidents of alarm
// @Description Allows to retrieve a page of incidents of an alarm, if alarm's target is public then the info can be retrieved by anyone, even without an auth token. This endpoint may return empty pages if alarm's target is not public.
// @Tags incident
// @Accept json
// @Produce json
// @Param id path uint true "ID of alarm to get the page of incidents for"
// @Param size query uint false "Size of the page, has to be a number in range [1, 200]. If value smaller or equal to 0 is given it defaults to 20. If value higher than 200 is given, 200 is used."
// @Param page query uint false "Page number, if a negative number is given it defaults to 0."
// @Param sort query string false "Sorting settings. Format: <field>,<direction> where field can be one of: (start, end duration, ongoing, incidents.id) and direction can be either asc for ascending or desc for descending. This param can be provided multiple times to sort by multiple columns at the same time."
// @Success 200 {object} getIncidentPageSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when a parameter couldn't be parsed."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /incident/alarm/{id}/page [get]
func HandleGetIncidentPageForAlarm(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		allowedFields := []string{"start", "end", "duration", "ongoing", "incidents.id"}
		pageable, err := helpers.ParsePageable(c, allowedFields)
		if err != nil {
			helpers.BadRequestWithSpecificError(c, err.Error())
			return
		}

		idParam := c.Param("id")
		id, err := strconv.ParseUint(idParam, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "alarm id")
			return
		}

		val, authenticatedUser := c.Get("user")
		var user model.User
		if authenticatedUser {
			user = val.(model.User)
		}

		query := db.Model(&model.Incident{}).Joins("Target").Joins("Alarm").Where(`"Alarm"."id" = ? and ("Target"."public" = true or ("Target"."user_id" = ? and ?))`, id, user.ID, authenticatedUser)
		page, err := helpers.ApplyPageable[model.Incident](query, pageable)
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: page,
		}
		resp.WriteAsJSON(c)
	}
}

type getIncidentPageSuccessResponse struct {
	Code int                          `json:"code" example:"200"`
	Data helpers.Page[model.Incident] `json:"data"`
}
