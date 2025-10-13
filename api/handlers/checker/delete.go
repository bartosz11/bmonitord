package checker

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleDeleteCheckerByID docs
// @Summary Delete a checker
// @Description Allows an admin to delete a checker
// @Tags checker
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param id path uint true "ID of checker that should be deleted"
// @Success 204 {object} helpers.GenericDeleteSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when user sending the request is not an admin or their account is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a checker with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /checker/{id} [delete]
func HandleDeleteCheckerByID(db *gorm.DB) gin.HandlerFunc {
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

		if checker.System {
			resp := helpers.HTTPResponse{
				Code:  http.StatusBadRequest,
				Error: "system checker cannot be deleted",
			}
			resp.WriteAsJSON(c)
			return
		}

		//Soft deletion here is not an accident
		if db.Delete(&checker).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusNoContent,
		}
		resp.WriteAsJSON(c)
	}
}
