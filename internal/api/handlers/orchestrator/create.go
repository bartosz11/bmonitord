package orchestrator

import (
	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/database/model"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"net/http"
	"strings"
)

// HandleCreateOrchestrator docs
// @Summary Create orchestrator
// @Description Allows an admin to create a new orchestrator
// @Tags orchestrator
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param createRequest body CreateOrchestratorRequest true "Information about new orchestrator"
// @Success 201 {object} createOrchestratorSuccessResponse
// @Failure 400 {object} helpers.GenericErrorResponse "Returned when request body doesn't match requirements."
// @Failure 401 {object} helpers.GenericErrorResponse "Returned when user sending the request supplies an invalid auth token."
// @Failure 403 {object} helpers.GenericErrorResponse "Returned when user sending the request is not an admin or their account is disabled."
// @Failure 409 {object} helpers.GenericErrorResponse "Returned when given name or host is already taken."
// @Failure 500 {object} helpers.GenericErrorResponse "Returned when a DB interaction fails."
// @Router /admin/orchestrator/ [post]
func HandleCreateOrchestrator(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		orchCreateReq := CreateOrchestratorRequest{}

		if c.ShouldBind(&orchCreateReq) != nil {
			helpers.BadRequestWithSpecificError(c, "invalid request body")
			return
		}

		if !strings.HasPrefix(orchCreateReq.Host, "ws://") && !strings.HasPrefix(orchCreateReq.Host, "wss://") {
			resp := helpers.HTTPResponse{
				Code:  http.StatusBadRequest,
				Error: "host must start with ws:// or wss://",
			}
			resp.WriteAsJSON(c)
			return
		}

		if strings.HasSuffix(orchCreateReq.Host, "/") {
			orchCreateReq.Host = strings.TrimSuffix(orchCreateReq.Host, "/")
		}

		if helpers.IsBlank(orchCreateReq.Name) {
			helpers.BadRequestWithSpecificError(c, "name must not be blank")
			return
		}

		var nameCount int64
		err := db.Model(&model.Orchestrator{}).Where("name = ?", orchCreateReq.Name).Count(&nameCount).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		if nameCount != 0 {
			resp := helpers.HTTPResponse{
				Code:  http.StatusConflict,
				Error: "name already taken",
			}
			resp.WriteAsJSON(c)
			return
		}

		var hostCount int64
		err = db.Model(&model.Orchestrator{}).Where("host = ?", orchCreateReq.Host).Count(&hostCount).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		if hostCount != 0 {
			resp := helpers.HTTPResponse{
				Code:  http.StatusConflict,
				Error: "host already taken",
			}
			resp.WriteAsJSON(c)
			return
		}

		orchestrator := model.Orchestrator{
			Name: orchCreateReq.Name,
			Host: orchCreateReq.Host,
		}

		err = db.Create(&orchestrator).Error
		if err != nil {
			helpers.DBInteractionFailed(c)
			return
		}

		resp := helpers.HTTPResponse{
			Code: http.StatusCreated,
			Data: orchestrator,
		}
		resp.WriteAsJSON(c)
	}
}

type CreateOrchestratorRequest struct {
	// Name must not be blank and must be unique
	Name string `json:"name" binding:"required"`
	// Host must start with ws:// or wss:// and must be unique
	Host string `json:"host" binding:"required"`
}

type createOrchestratorSuccessResponse struct {
	Code int                `json:"code" example:"201"`
	Data model.Orchestrator `json:"data"`
}
