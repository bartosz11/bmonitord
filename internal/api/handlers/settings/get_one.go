package settings

import (
	"errors"
	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

// HandleGetSettingByKey docs
// @Summary Get setting by key
// @Description Allows an admin to retrieve a setting by key
// @Tags settings
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param key path string true "Setting to retrieve"
// @Success 200 {object} getSettingSuccessResponse
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when user sending the request is not an admin or their account is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a setting with given key couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /admin/settings/:key [get]
func HandleGetSettingByKey(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		key := c.Param("key")

		var setting model.Setting
		err := db.First(&setting, "key = ?", key).Error

		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: setting,
		}
		resp.WriteAsJSON(c)
	}
}

type getSettingSuccessResponse struct {
	Code int           `json:"code" example:"200"`
	Data model.Setting `json:"data"`
}
