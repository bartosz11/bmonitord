package session

import (
	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

// HandleGetAllSessions docs
// @Summary Get all sessions
// @Description Allows a user to retrieve a list of all their sessions
// @Tags session
// @Security BearerAuth
// @Accept json
// @Produce json
// @Success 200 {object} listSessionsSuccessResponse
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /session/ [get]
func HandleGetAllSessions(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		var sessions []model.Session
		err := db.Joins("User").Find(&sessions, "user_id = ?", user.ID).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: sessions,
		}
		resp.WriteAsJSON(c)
	}
}

type listSessionsSuccessResponse struct {
	Code int             `json:"code" example:"200"`
	Data []model.Session `json:"data"`
}
