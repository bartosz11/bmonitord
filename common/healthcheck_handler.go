package common

import (
	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleHealthcheck docs
// @Summary Check health
// @Description Allows to check if orchestrator's/API's connection to the DB is OK
// @Tags health
// @Produce json
// @Success 200 {object} healthcheckSuccessResponse
// @Failure 500 {object} healthcheckFailResponse
// @Router /api/health [get]
// @Router /orchestrator/health [get]
func HandleHealthcheck(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		resp := helpers.HTTPResponse{
			Code: 200,
			Data: gin.H{
				"db": "ok",
			},
		}

		sqlDB, err := db.DB()
		if err != nil || sqlDB.Ping() != nil {
			resp.Code = 500
			resp.Data = gin.H{
				"db": "error",
			}
		}

		resp.WriteAsJSON(c)
	}
}

type healthcheckSuccessResponse struct {
	Code int `json:"code" example:"200"`
	Data struct {
		DB string `json:"db" example:"ok"`
	} `json:"data"`
}

type healthcheckFailResponse struct {
	Code int `json:"code" example:"500"`
	Data struct {
		DB string `json:"db" example:"error"`
	} `json:"data"`
}
