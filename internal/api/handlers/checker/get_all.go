package checker

import (
	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

// HandleGetAllCheckers docs
// @Summary Get all checkers
// @Description Allows an admin to get a list of checkers
// @Tags checker
// @Security BearerAuth
// @Accept json
// @Produce json
// @Success 200 {object} listCheckersSuccessResponse
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when user sending the request is not an admin or their account is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /admin/checker/ [get]
func HandleGetAllCheckers(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		var checkers []model.Checker

		if db.Find(&checkers).Error != nil {
			helpers.DBInteractionFailed(c)
			return
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
