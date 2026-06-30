package group

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleCreateStatuspageGroup docs
// @Summary Create a statuspage group
// @Description Allows a user to create a group for targets in a statuspage.
// @Tags statuspageGroup
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage"
// @Param createReq body CreateStatuspageGroupRequest true "Information about new statuspage group"
// @Success 201 {object} createStatuspageGroupSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body doesn't match requirements or statuspage ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId}/group [post]
func HandleCreateStatuspageGroup(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("statuspageId")
		statuspageId, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "statuspage id")
			return
		}

		var statuspage model.Statuspage
		err = db.First(&statuspage, "id = ? and user_id = ?", statuspageId, user.ID).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		var createReq CreateStatuspageGroupRequest
		if c.ShouldBind(&createReq) != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
			return
		}

		if helpers.IsBlank(createReq.Name) {
			helpers.BadRequestWithSpecificError(c, "name cannot be blank")
			return
		}

		pos := float64(0)
		if createReq.Position != nil {
			if *createReq.Position < 0 {
				helpers.BadRequestWithSpecificError(c, "position must be a non-negative number")
				return
			}
			pos = *createReq.Position
		}

		group := model.StatuspageGroup{
			Name:         createReq.Name,
			Description:  createReq.Description,
			Position:     pos,
			StatuspageID: statuspage.ID,
		}

		if db.Save(&group).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusCreated,
			Data: group,
		}
		resp.WriteAsJSON(c)
	}
}

type CreateStatuspageGroupRequest struct {
	// Name cannot be empty
	Name string `json:"name" binding:"required"`
	// Description is optional
	Description string `json:"description"`
	// Position is optional, must be a non-negative value if provided. Value of 0 is persisted if not provided.
	Position *float64 `json:"position"`
}

type createStatuspageGroupSuccessResponse struct {
	Code int                   `json:"code" example:"200"`
	Data model.StatuspageGroup `json:"data"`
}
