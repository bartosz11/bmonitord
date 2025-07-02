package api

import (
	"bmonitord/config"
	"bmonitord/internal/api/handlers/auth"
	"bmonitord/internal/api/handlers/orchestrator"
	"bmonitord/internal/api/handlers/session"
	"bmonitord/internal/api/handlers/user"
	"bmonitord/internal/api/helpers"
	"bmonitord/internal/api/middleware"
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"time"
)

func StartAPI(db *gorm.DB, router *gin.Engine, production bool, apiConfig *config.APIConfig) {
	apiGroup := router.Group("/api")
	if !production {
		apiGroup.Use(cors.New(cors.Config{
			AllowOrigins:     []string{"http://localhost:5173"},
			AllowMethods:     []string{"PUT", "PATCH", "GET", "OPTIONS", "HEAD", "POST", "DELETE"},
			AllowHeaders:     []string{"Origin"},
			ExposeHeaders:    []string{"Content-Length"},
			AllowCredentials: true,
			MaxAge:           12 * time.Hour,
		}))
	}

	helpers.InitJWTHelper(apiConfig)

	authGrp := apiGroup.Group("/auth")
	{
		authGrp.POST("/login", auth.HandleLogin(db))
		authGrp.POST("/register", auth.HandleRegister(db))
	}

	restrictedGrp := apiGroup.Group("", middleware.AuthMiddleware(db))
	{
		userGrp := restrictedGrp.Group("/user")
		{
			userGrp.GET("/", user.HandleGetCurrentUser())
			userGrp.PATCH("/username", user.HandleChangeUsername(db))
			userGrp.PATCH("/password", user.HandleChangePassword(db))
			userGrp.DELETE("/", user.HandleDeleteCurrentUser(db))
		}
		sessionGrp := restrictedGrp.Group("/session")
		{
			sessionGrp.POST("/logout", session.HandleLogout(db))
			sessionGrp.GET("/", session.HandleGetAllSessions(db))
			sessionGrp.GET("/:id", session.HandleGetSession(db))
			sessionGrp.GET("/current", session.HandleGetCurrentSession())
			sessionGrp.DELETE("/", session.HandleDeleteAllSessions(db))
			sessionGrp.DELETE("/:id", session.HandleDeleteSession(db))
		}
	}

	adminGrp := restrictedGrp.Group("/admin", middleware.AdminMiddleware())
	{
		orchestratorGrp := adminGrp.Group("/orchestrator")
		{
			orchestratorGrp.POST("/", orchestrator.HandleCreateOrchestrator(db))
			orchestratorGrp.DELETE("/:id", orchestrator.HandleDeleteOrchestrator(db))
			orchestratorGrp.GET("/:id", orchestrator.HandleGetOrchestratorByID(db))
			orchestratorGrp.GET("/", orchestrator.HandleGetAllOrchestrators(db))
			//Orchestrator details aren't meant to be updated - might cause checkers to fail; I guess
			//Might implement such an endpoint for that later on if I see it as reasonable,
			//Ideally all updates to the whole orchestrators table should get broadcasted to all checkers, but it's kind of impossible with the current "stateless API" structure, I may be looking for a workaround in the future
		}
	}
}
