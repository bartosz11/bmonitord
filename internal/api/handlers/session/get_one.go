package session

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
	"strconv"
)

// HandleGetSession docs
// @Summary Get session by ID
// @Description Allows a user to retrieve information about specified session.
// @Tags session
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param id path uint true "ID of session to retrieve"
// @Success 200 {object} getSessionSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when provided session ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when session with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /session/:id [get]
func HandleGetSession(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("id")
		id, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "session id")
			return
		}

		var count int64
		err = db.Model(&model.Session{}).Where("user_id = ? and id = ?", user.ID, id).Count(&count).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}
		if count == 0 {
			helpers.NotFound(c)
			return
		}

		var session model.Session
		err = db.Joins("User").Find(&session, "user_id = ? and sessions.id = ?", user.ID, id).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: session,
		}
		resp.WriteAsJSON(c)
	}
}

type getSessionSuccessResponse struct {
	Code int           `json:"code" example:"200"`
	Data model.Session `json:"data"`
}
