package statuspage

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// HandleUpdateStatuspageById docs
// @Summary Update a statuspage
// @Description Allows a user to update a statuspage.
// @Tags statuspage
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param statuspageId path uint true "ID of statuspage to update"
// @Param updateReq body UpdateStatuspageRequest true "New information about statuspage"
// @Success 200 {object} getStatuspageSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body doesn't match requirements or statuspage ID couldn't be parsed."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when account of user sending the request is disabled."
// @Failure 404 {object} helpers.GenericErrorResponse "Returned when a statuspage with given ID couldn't be found."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /statuspage/{statuspageId} [patch]
func HandleUpdateStatuspageById(db *gorm.DB) gin.HandlerFunc {
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
		err = db.First(&statuspageId, "id = ? and user_id = ?", statuspageId, user.ID).Error

		if errors.Is(err, gorm.ErrRecordNotFound) {
			helpers.NotFound(c)
			return
		}

		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		updateReq := UpdateStatuspageRequest{}
		if c.ShouldBind(&updateReq) != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
			return
		}

		newName := updateReq.Name
		if newName != nil {
			if helpers.IsBlank(*newName) {
				helpers.BadRequestWithSpecificError(c, "name cannot be blank")
				return
			}

			statuspage.Name = *newName
		}

		newSlug := updateReq.Slug
		if newSlug != nil {
			if !helpers.IsValidStatuspageSlug(*newSlug) {
				helpers.BadRequestWithSpecificError(c, "invalid slug")
			}

			var count int64
			if db.Model(model.Statuspage{}).Where("slug = ?", *newSlug).Count(&count).Error != nil {
				helpers.DBInteractionFailed(c)
				return
			}

			if count != 0 {
				helpers.BadRequestWithSpecificError(c, "given slug is already taken")
				return
			}

			statuspage.Slug = *newSlug
		}

		newTitle := updateReq.Title
		if newTitle != nil {
			if helpers.IsBlank(*newTitle) {
				helpers.BadRequestWithSpecificError(c, "title cannot be blank")
				return
			}

			statuspage.Title = *newTitle
		}

		newDescription := updateReq.Description
		if newDescription != nil {
			statuspage.Description = *newDescription
		}

		newLogoURL := updateReq.LogoURL
		if newLogoURL != nil {
			statuspage.LogoURL = *newLogoURL
		}

		newTitleSectionOnClickURL := updateReq.TitleSectionOnClickURL
		if newTitleSectionOnClickURL != nil {
			statuspage.TitleSectionOnClickURL = *newTitleSectionOnClickURL
		}

		newFooterContent := updateReq.FooterContent
		if newFooterContent != nil {
			statuspage.FooterContent = *newFooterContent
		}

		displayCheckmateFooter := updateReq.DisplayCheckmateFooter
		if displayCheckmateFooter != nil {
			statuspage.DisplayCheckmateFooter = *displayCheckmateFooter
		}

		err = db.Save(&statuspage).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusOK,
			Data: statuspage,
		}
		resp.WriteAsJSON(c)
	}
}

type UpdateStatuspageRequest struct {
	// Name is only displayed in the dashboard, must not be blank if provided
	Name *string `json:"name"`
	// Slug is used to route viewers to statuspages. Given slug must not be used across this instance already. Must not be blank if provided
	// A slug is deemed invalid if it is empty, starts or ends with a hyphen, contains characters other than lowercase letters, digits, or hyphens, or includes consecutive hyphens.
	Slug *string `json:"slug"`
	// Title is displayed on top of the statuspage, must not be blank if provided
	Title *string `json:"title"`
	// Description is displayed below the title, can be blank to reset
	Description *string `json:"description"`
	// LogoURL's value is used to display a logo left of the title, can be blank to reset
	LogoURL *string `json:"logoURL"`
	// TitleSectionOnClickURL's value is where the title section (title and logo) will redirect when the user clicks on it. Clicking will do nothing if this value is empty. Can be set to an empty string to reset.
	TitleSectionOnClickURL *string `json:"titleSectionOnClickURL"`
	// FooterContent is the text displayed in the footer area, can be blank to reset
	FooterContent *string `json:"footerContent"`
	// DisplayCheckmateFooter specifies whether a "Powered by checkmate" footer is displayed at the bottom of the page.
	DisplayCheckmateFooter *bool `json:"displayCheckmateFooter"`
}
