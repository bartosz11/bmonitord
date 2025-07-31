package user

import (
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
)

// HandleDeleteCurrentUser docs
// @Summary Delete current user
// @Description Allows user to delete their own account and all resources they own
// @Tags user
// @Security BearerAuth
// @Accept json
// @Produce json
// @Success 204 {object} helpers.GenericDeleteSuccessResponse
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /user/ [delete]
func HandleDeleteCurrentUser(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		value, _ := c.Get("user")
		user := value.(model.User)

		//There's pretty much no point in soft deleting anything other than checkers
		//since everything "DELETE CASCADE"s I can just do this:
		err := db.Unscoped().Delete(&user).Error
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
