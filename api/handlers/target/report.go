package target

import (
	"errors"
	"net/http"
	"strconv"
	"time"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleGetTargetReportData docs
// @Summary Get target's report data by ID
// @Description Allows to retrieve data for report about target with specified ID, if target is public then the info can be retrieved by anyone, even without an auth token
// @Tags target
// @Accept json
// @Produce json
// @Param targetID path uint true "ID of target to get the data for"
// @Success 200 {object} getTargetReportDataSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given ID couldn't be parsed."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a target with given ID couldn't be found or user sending the request isn't allowed to access it (target isn't public)."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /target/{targetID}/report [get]
func HandleGetTargetReportData(db *gorm.DB) gin.HandlerFunc {
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
		err = db.First(&target, "id = ? and (public = true or (user_id = ? and ?))", id, user.ID, authenticatedUser).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		reportData := ReportData{
			Target:                      target,
			LastHeartbeatsFromLocations: make([]model.Heartbeat, 0),
		}
		// We technically can base this off of "hbs" but I don't think it's that big of a deal
		var lastHbsPerLocation []model.Heartbeat
		err = db.Select("DISTINCT ON (heartbeats.checker_id) heartbeats.*").
			Preload("Checker", func(db *gorm.DB) *gorm.DB {
				return db.Omit("key")
			}).
			Order("checker_id, timestamp desc").
			Find(&lastHbsPerLocation, "target_id = ?", target.ID).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}
		reportData.LastHeartbeatsFromLocations = lastHbsPerLocation

		var incidents []model.Incident
		err = db.Joins("Alarm").Order("start").Find(&incidents, "incidents.target_id = ?", target.ID).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		now := time.Now()
		thirtyDaysAgo := now.AddDate(0, 0, -30)

		uptime := UptimeData{
			Overall:  CalculateUptime(target.CreatedAt, now, incidents),
			Last24h:  CalculateUptime(now.AddDate(0, 0, -1), now, incidents),
			Last7d:   CalculateUptime(now.AddDate(0, 0, -7), now, incidents),
			Last30d:  CalculateUptime(thirtyDaysAgo, now, incidents),
			Last365d: CalculateUptime(now.AddDate(0, 0, -365), now, incidents),
		}
		reportData.Uptime = uptime

		var last30dIncidents []model.Incident
		for _, incident := range incidents {
			if incident.Ongoing || incident.End.After(thirtyDaysAgo) {
				last30dIncidents = append(last30dIncidents, incident)
			}
		}
		if len(last30dIncidents) == 0 {
			reportData.IncidentHistory = make([]model.Incident, 0)
		} else {
			helpers.Reverse(last30dIncidents)
			reportData.IncidentHistory = last30dIncidents
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: reportData,
		}
		resp.WriteAsJSON(c)
	}
}

type ReportData struct {
	// Target is the target the report was requested for
	Target model.Target `json:"target"`
	// Uptime contains pre-computed uptime percentages for various time periods based on incidents in these time periods
	Uptime UptimeData `json:"uptime"`
	// IncidentHistory contains incidents from last 30 days
	IncidentHistory []model.Incident `json:"incidentHistory"`
	// LastHeartbeatsFromLocations contains most recent heartbeats from all locations, one per location or the last heartbeat if target is of a type that's push
	LastHeartbeatsFromLocations []model.Heartbeat `json:"lastHeartbeatsFromLocations"`
}

type UptimeData struct {
	Overall  float64 `json:"overall" example:"99.573"`
	Last24h  float64 `json:"last_24h" example:"99.572"`
	Last7d   float64 `json:"last_7d" example:"99.573"`
	Last30d  float64 `json:"last_30d" example:"99.573"`
	Last365d float64 `json:"last_365d" example:"99.573"`
}

type getTargetReportDataSuccessResponse struct {
	Code int        `json:"code" example:"200"`
	Data ReportData `json:"data"`
}
