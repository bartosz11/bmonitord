package api

import (
	"github.com/bartosz11/checkmate/config"
	_ "github.com/bartosz11/checkmate/docs"
	"github.com/bartosz11/checkmate/internal/api/handlers/auth"
	"github.com/bartosz11/checkmate/internal/api/handlers/checker"
	"github.com/bartosz11/checkmate/internal/api/handlers/notification"
	"github.com/bartosz11/checkmate/internal/api/handlers/orchestrator"
	"github.com/bartosz11/checkmate/internal/api/handlers/session"
	"github.com/bartosz11/checkmate/internal/api/handlers/settings"
	"github.com/bartosz11/checkmate/internal/api/handlers/target"
	"github.com/bartosz11/checkmate/internal/api/handlers/target/alarm"
	"github.com/bartosz11/checkmate/internal/api/handlers/user"
	"github.com/bartosz11/checkmate/internal/api/helpers"
	"github.com/bartosz11/checkmate/internal/api/middleware"
	"time"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/gin-gonic/gin/binding"
	"github.com/go-playground/validator/v10"
	"github.com/rs/zerolog/log"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"
	"gorm.io/gorm"
)

//	@title			checkmate API
//	@version		1.0
//	@description	API docs for checkmate
//	@host			localhost:8080
//	@BasePath		/api
//
//  @securityDefinitions.apikey BearerAuth
//  @in header
//  @name Authorization
//  @description Header value has to start with "Bearer "

func StartAPI(db *gorm.DB, router *gin.Engine, production bool, apiConfig *config.APIConfig) {
	if v, ok := binding.Validator.Engine().(*validator.Validate); ok {
		err := v.RegisterValidation("requiredUint", helpers.ValidateRequiredUint)
		if err != nil {
			log.Fatal().Err(err).Msg("failed to register custom uint validator")
		}
	}

	if apiConfig.HostDocs {
		router.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))
	}

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
		authGrp.POST("/login", auth.HandleLogin(db, &apiConfig.SecureCookies))
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
		notificationGrp := restrictedGrp.Group("/notification")
		{
			notificationGrp.POST("/", notification.HandleCreateNotification(db))
			notificationGrp.DELETE("/:id", notification.HandleDeleteNotificationById(db))
			notificationGrp.GET("/:id", notification.HandleGetNotificationById(db))
			notificationGrp.GET("/", notification.HandleGetUsersNotifications(db))
			notificationGrp.PATCH("/:id", notification.HandleUpdateNotificationById(db))
			notificationGrp.POST("/:id/test", notification.HandleSendTestNotification(db))
		}
		targetGrp := restrictedGrp.Group("/target")
		{
			targetGrp.POST("/", target.HandleCreateTarget(db))
			targetGrp.GET("/", target.HandleGetAllUsersTargets(db))
			//if it's not done like this gin panics, there may be a better fix
			specificTargetGrp := targetGrp.Group("/:targetID")
			{
				specificTargetGrp.DELETE("", target.HandleDeleteTargetByID(db))
				specificTargetGrp.GET("", target.HandleGetTargetByID(db))
				specificTargetGrp.PATCH("", target.HandleUpdateTargetById(db))
				specificTargetGrp.PATCH("/pause", target.HandlePauseTarget(db))
				alarmGrp := specificTargetGrp.Group("/alarm")
				{
					alarmGrp.POST("/", alarm.HandleCreateAlarm(db))
					alarmGrp.GET("/", alarm.HandleGetAllAlarmsByTargetID(db))
					alarmGrp.GET("/:alarmID", alarm.HandleGetAlarmByID(db))
					alarmGrp.DELETE("/:alarmID", alarm.HandleDeleteAlarmByID(db))
					alarmGrp.PATCH("/:alarmID", alarm.HandleUpdateAlarm(db))
					alarmGrp.PATCH("/:alarmID/mute", alarm.HandleMuteAlarm(db))
				}
			}
		}
	}
	//TODO: separate incident and heartbeat groups - read only data that might be public at some point
	// so that's why it should be on separate group, there's no point in making things like /target/:id/heartbeat[s]/:id
	// they're theoretically child entities, but the "publicity" makes them kind of independent

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
		checkerGrp := adminGrp.Group("/checker")
		{
			checkerGrp.POST("/", checker.HandleCreateChecker(db))
			checkerGrp.DELETE("/:id", checker.HandleDeleteCheckerByID(db))
			checkerGrp.GET("/", checker.HandleGetAllCheckers(db))
			checkerGrp.GET("/:id", checker.HandleGetCheckerByID(db))
			checkerGrp.PATCH("/:id", checker.HandleUpdateChecker(db))
			checkerGrp.PATCH("/:id/key", checker.HandleRegenCheckerKey(db))
		}
		settingsGrp := adminGrp.Group("/settings")
		{
			//Update endpoint also serves as create endpoint, in this case I think it makes sense
			settingsGrp.POST("/", settings.HandleChangeSetting(db))
			settingsGrp.GET("/", settings.HandleGetAllSettings(db))
			settingsGrp.GET("/:key", settings.HandleGetSettingByKey(db))
			//There will be no delete endpoint - if I ever need to delete settings, it'll be done through migrations in an "if exists" style
		}
	}
}
