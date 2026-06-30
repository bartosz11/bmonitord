package statuspage

import (
	"net/http"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleGetAllStatuspages docs
// @Summary Get all statuspages
// @Description Allows a user to get a list of their statuspages
// @Tags statuspage
// @Security BearerAuth
// @Accept json
// @Produce json
// @Success 200 {object} listStatuspagesSuccessResponse
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/ [get]
func HandleGetAllStatuspages(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		v, _ := c.Get("user")
		user := v.(model.User)

		var statuspages []model.Statuspage
		if db.Find(&statuspages, "user_id = ?", user.ID).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: statuspages,
		}
		resp.WriteAsJSON(c)
	}
}

type listStatuspagesSuccessResponse struct {
	Code int                `json:"code" example:"200"`
	Data []model.Statuspage `json:"data"`
}
