package statuspage

import (
	"net/http"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleStatuspageCreate docs
// @Summary Create a statuspage
// @Description Allows a user to create a statuspage.
// @Tags statuspage
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param createReq body CreateStatuspageRequest true "Information about new statuspage"
// @Success 201 {object} createStatuspageSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body doesn't match requirements."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/ [post]
func HandleStatuspageCreate(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		val, _ := c.Get("user")
		user := val.(model.User)

		createReq := CreateStatuspageRequest{}

		err := c.ShouldBind(&createReq)
		if err != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
			return
		}

		if helpers.IsBlank(createReq.Name) {
			helpers.BadRequestWithSpecificError(c, "name must not be blank")
			return
		}

		if !helpers.IsValidStatuspageSlug(createReq.Slug) {
			helpers.BadRequestWithSpecificError(c, "invalid slug")
			return
		}

		var count int64
		if db.Model(model.Statuspage{}).Where("slug = ?", createReq.Slug).Count(&count).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		if count != 0 {
			helpers.BadRequestWithSpecificError(c, "given slug is already taken")
			return
		}

		if helpers.IsBlank(createReq.Title) {
			helpers.BadRequestWithSpecificError(c, "title must not be blank")
			return
		}

		statuspage := model.Statuspage{
			Name:                   createReq.Name,
			Slug:                   createReq.Slug,
			Title:                  createReq.Title,
			Description:            createReq.Description,
			LogoURL:                createReq.LogoURL,
			TitleSectionOnClickURL: createReq.TitleSectionOnClickURL,
			FooterContent:          createReq.FooterContent,
			DisplayCheckmateFooter: createReq.DisplayCheckmateFooter,
			UserID:                 user.ID,
		}

		if db.Create(&statuspage).Error != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusCreated,
			Data: statuspage,
		}
		resp.WriteAsJSON(c)
	}
}

type CreateStatuspageRequest struct {
	// Name is only displayed in the dashboard, must not be blank
	Name string `json:"name" binding:"required"`
	// Slug is used to route viewers to statuspages. Given slug must not be used across this instance already.
	// A slug is deemed invalid if it is empty, starts or ends with a hyphen, contains characters other than lowercase letters, digits, or hyphens, or includes consecutive hyphens.
	Slug string `json:"slug" binding:"required"`
	// Title is displayed on top of the statuspage, must not be blank
	Title string `json:"title" binding:"required"`
	// Description is displayed below the title, it's optional
	Description string `json:"description"`
	// LogoURL's value is used to display a logo left of the title, it's optional
	LogoURL string `json:"logoURL"`
	// TitleSectionOnClickURL's value is where the title section (title and logo) will redirect when the user clicks on it. If not supplied, clicking will do nothing
	TitleSectionOnClickURL string `json:"titleSectionOnClickURL"`
	// FooterContent is the text displayed in the footer area, it's optional
	FooterContent string `json:"footerContent"`
	// DisplayCheckmateFooter specifies whether a "Powered by checkmate" footer is displayed at the bottom of the page.
	DisplayCheckmateFooter bool `json:"displayCheckmateFooter"`
}

type createStatuspageSuccessResponse struct {
	Code int              `json:"code" example:"201"`
	Data model.Statuspage `json:"data"`
}
