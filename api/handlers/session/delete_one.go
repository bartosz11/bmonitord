package session

import (
	"net/http"
	"strconv"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleDeleteSession docs
// @Summary Delete (invalidate) session by ID
// @Description Allows a user to invalidate a specified session
// @Tags session
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param id path uint true "ID of session to delete"
// @Success 204 {object} helpers.GenericDeleteSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when provided session ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when session with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /session/{id} [delete]
func HandleDeleteSession(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		param := c.Param("id")
		id, err := strconv.ParseUint(param, 10, 64)
		if err != nil {
			helpers.ParsingFailed(c, "session id")
			return
		}

		var count int64
		err = db.Model(&model.Session{}).Where("user_id = ? and id = ?", user.ID, id).Count(&count).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}
		if count == 0 {
			helpers.NotFound(c)
			return
		}

		err = db.Unscoped().Delete(&model.Session{}, "user_id = ? and id = ?", user.ID, id).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusNoContent,
		}
		resp.WriteAsJSON(c)
	}
}
