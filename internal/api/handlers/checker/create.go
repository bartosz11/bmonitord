package checker

import (
	"net/http"

	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleCreateChecker docs
// @Summary Create a new checker
// @Description Allows an admin to create a checker
// @Tags checker
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param checkerInfo body CreateCheckerRequest true "Data required to create a new checker"
// @Success 201 {object} createCheckerSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body does not match the requirements."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when user sending the request is not an admin or their account is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /admin/checker/ [post]
func HandleCreateChecker(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		checkerCreateReq := CreateCheckerRequest{}

		if c.ShouldBind(&checkerCreateReq) != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
			return
		}

		if helpers.IsBlank(checkerCreateReq.Name) {
			helpers.BadRequestWithSpecificError(c, "checker name cannot be blank")
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

type CreateCheckerRequest struct {
	// Name cannot be blank
	Name     string `json:"name" binding:"required"`
	Location string `json:"location"`
}

type createCheckerSuccessResponse struct {
	Code int           `json:"code" example:"201"`
	Data model.Checker `json:"data"`
}
