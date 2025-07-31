package session

import (
	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

// HandleDeleteAllSessions docs
// @Summary Delete (invalidate) all sessions
// @Description Allows a user to invalidate all their login sessions
// @Tags session
// @Security BearerAuth
// @Accept json
// @Produce json
// @Success 204 {object} helpers.GenericDeleteSuccessResponse
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /session/ [delete]
func HandleDeleteAllSessions(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		err := db.Unscoped().Delete(&model.Session{}, "user_id = ?", user.ID).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusNoContent,
		}
		resp.WriteAsJSON(c)
	}
}
