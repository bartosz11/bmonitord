package checker

import (
	"errors"
	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
	"strconv"
)

// HandleRegenCheckerKey docs
// @Summary Re-generate checker's key
// @Description Allows an admin to generate a new key for a checker
// @Tags checker
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param id path uint true "ID of checker that should get a new key"
// @Success 200 {object} getCheckerSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when user sending the request is not an admin or their account is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a checker with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /admin/checker/:id/key [patch]
func HandleRegenCheckerKey(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		param := c.Param("id")
		checkerID, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "checker id")
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

		key := GenerateUniqueKey(db, c)
		if key == "" {
			return
		}

		checker.Key = key
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
