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
			c.JSON(http.StatusForbidden, gin.H{
				"error": "this setting cannot be modified",
			})
			return
		}

		if !validateSetting(changeSettingReq) {
			c.JSON(http.StatusBadRequest, gin.H{
				"error": "invalid value for given setting",
			})
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

		c.JSON(http.StatusOK, gin.H{
			"setting": setting,
		})
	}
}
