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

// HandleUpdateChecker docs
// @Summary Update checker details
// @Description Allows an admin to change checker's name or location
// @Tags checker
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param id path uint true "ID of checker that should be updated"
// @Param checkerInfo body UpdateCheckerRequest true "New checker details"
// @Success 200 {object} getCheckerSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when given ID couldn't be parsed or given new name is blank."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when user sending the request is not an admin or their account is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a checker with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /checker/{id} [patch]
func HandleUpdateChecker(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		param := c.Param("id")
		checkerID, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "checker id")
			return
		}

		checkerUpdateReq := UpdateCheckerRequest{}

		if c.ShouldBind(&checkerUpdateReq) != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
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
			if helpers.IsBlank(*checkerUpdateReq.Name) {
				helpers.BadRequestWithSpecificError(c, "name cannot be blank if supplied")
				return
			}
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

type UpdateCheckerRequest struct {
	// Name must not be blank if supplied
	Name *string `json:"name,omitempty"`
	// Location can be blank
	Location *string `json:"location,omitempty"`
}
