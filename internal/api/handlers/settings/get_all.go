package settings

import (
	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

// HandleGetAllSettings docs
// @Summary Get all settings
// @Description Allows an admin to retrieve list of all settings
// @Tags settings
// @Security BearerAuth
// @Accept json
// @Produce json
// @Success 200 {object} listSettingsSuccessResponse
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when user sending the request is not an admin or their account is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /admin/settings/ [get]
func HandleGetAllSettings(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		var settings []model.Setting
		err := db.Find(&settings).Error

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: settings,
		}
		resp.WriteAsJSON(c)
	}
}

type listSettingsSuccessResponse struct {
	Code int             `json:"code" example:"200"`
	Data []model.Setting `json:"data"`
}
