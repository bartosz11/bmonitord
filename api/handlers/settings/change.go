package settings

import (
	"net/http"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

type ChangeSettingRequest struct {
	// Key must be updatable. Currently, the only setting that can be updated is "registration-enabled".
	Key string `json:"key" binding:"required"`
	// Value must match a set of valid values for given setting key
	Value *string `json:"value"`
}

// HandleChangeSetting docs
// @Summary Set/modify setting value
// @Description Allows an admin to set/update a setting
// @Tags settings
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param changeRequest body ChangeSettingRequest true "Setting to update"
// @Success 200 {object} getSettingSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body is malformed or value isn't valid for given setting key."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when user sending the request is not an admin, their account is disabled or specified setting is not meant to be updated."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /admin/settings/ [post]
func HandleChangeSetting(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		changeSettingReq := ChangeSettingRequest{}

		if c.ShouldBind(&changeSettingReq) != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
			return
		}

		if !isModifiable(changeSettingReq.Key) {
			resp := helpers.HTTPResponse{
				Code:  http.StatusForbidden,
				Error: "this setting cannot be modified",
			}
			resp.WriteAsJSON(c)
			return
		}

		if !validateSetting(changeSettingReq) {
			resp := helpers.HTTPResponse{
				Code:  http.StatusBadRequest,
				Error: "invalid value for given setting",
			}
			resp.WriteAsJSON(c)
			return
		}

		setting := model.Setting{
			Key:   changeSettingReq.Key,
			Value: changeSettingReq.Value,
		}

		if db.Save(&setting).Error != nil {
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
