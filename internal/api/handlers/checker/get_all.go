package checker

import (
	"net/http"

	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleGetAllCheckers docs
// @Summary Get all checkers
// @Description Allows a user to get list of all checkers, includes keys if requesting user has admin privileges
// @Tags checker
// @Security BearerAuth
// @Accept json
// @Produce json
// @Success 200 {object} listCheckersSuccessResponse
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /checker/ [get]
func HandleGetAllCheckers(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		value, _ := c.Get("user")
		user := value.(model.User)
		var checkers []model.Checker

		if db.Find(&checkers).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		if !user.Admin {
			// Make sure we don't leak the keys for the non-admin users
			SanitizeCheckers(&checkers)
		}
		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: checkers,
		}
		resp.WriteAsJSON(c)
	}
}

type listCheckersSuccessResponse struct {
	Code int             `json:"code" example:"200"`
	Data []model.Checker `json:"data"`
}
