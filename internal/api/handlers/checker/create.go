package checker

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

func HandleCreateChecker(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		checkerCreateReq := struct {
			Name     string `json:"name" binding:"required"`
			Location string `json:"location"`
		}{}

		if c.ShouldBind(&checkerCreateReq) != nil {
			helpers.BadRequest(c)
			return
		}

		if helpers.IsBlank(checkerCreateReq.Name) {
			helpers.BadRequest(c)
			return
		}

		key := GenerateUniqueKey(db, c)
		if key == "" {
			//If this happened, a DB error has occurred and the client has been informed already, so return only
			return
		}

		checker := model.Checker{
			Name:     checkerCreateReq.Name,
			Location: checkerCreateReq.Location,
			Key:      key,
		}

		err := db.Create(&checker).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusCreated,
			Data: checker,
		}
		resp.WriteAsJSON(c)
	}
}

func GenerateUniqueKey(db *gorm.DB, c *gin.Context) string {
	var key string
	//Always generate a unique key
	for {
		key = helpers.GenerateRandomAlphanumericString(20)
		var count int64

		err := db.Model(&model.Checker{}).Where("key = ?", key).Count(&count).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return ""
		}

		if count == 0 {
			break
		}
	}
	return key
}
