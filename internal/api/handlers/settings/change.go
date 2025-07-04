package settings

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

type ChangeSettingRequest struct {
	Key   string  `json:"key" binding:"required"`
	Value *string `json:"value"`
}

func HandleChangeSetting(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		changeSettingReq := ChangeSettingRequest{}

		if c.ShouldBind(&changeSettingReq) != nil {
			helpers.BadRequest(c)
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
