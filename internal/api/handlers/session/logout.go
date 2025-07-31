package session

import (
	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

// HandleLogout docs
// @Summary Log out
// @Description Allows a user to delete (invalidate) the session assigned to supplied auth token.
// @Tags session
// @Security BearerAuth
// @Accept json
// @Produce json
// @Success 200 {object} helpers.GenericDeleteSuccessResponse
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /session/logout [post]
func HandleLogout(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		session, _ := c.Get("session")
		err := db.Unscoped().Delete(&session).Error
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
