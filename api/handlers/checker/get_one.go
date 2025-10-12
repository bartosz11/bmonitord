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

// HandleGetCheckerByID docs
// @Summary Get a checker by ID
// @Description Allows a user to get a checker by its ID, includes key if user has admin privileges
// @Tags checker
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param id path uint true "ID of checker that should be retrieved"
// @Success 200 {object} getCheckerSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a checker with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /checker/{id} [get]
func HandleGetCheckerByID(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		value, _ := c.Get("user")
		user := value.(model.User)

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

		if !user.Admin {
			SanitizeChecker(&checker)
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: checker,
		}
		resp.WriteAsJSON(c)
	}
}

type getCheckerSuccessResponse struct {
	Code int           `json:"code" example:"200"`
	Data model.Checker `json:"data"`
}
