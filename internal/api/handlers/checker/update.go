package checker

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"errors"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
	"strconv"
)

func HandleUpdateChecker(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		param := c.Param("id")
		checkerID, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "checker id")
			return
		}

		checkerUpdateReq := struct {
			Name     *string `json:"name,omitempty"`
			Location *string `json:"location,omitempty"`
		}{}

		if c.ShouldBind(&checkerUpdateReq) != nil {
			helpers.BadRequest(c)
			return
		}

		var checker model.Checker
		err = db.First(&checker, "id = ?", checkerID).Error

		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		if checkerUpdateReq.Name != nil {
			checker.Name = *checkerUpdateReq.Name
		}

		if checkerUpdateReq.Location != nil {
			checker.Location = *checkerUpdateReq.Location
		}

		if db.Save(&checker).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: checker,
		}
		resp.WriteAsJSON(c)
	}
}
