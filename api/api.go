package api

import (
	_ "embed"
	"time"

	"github.com/bartosz11/checkmate/api/handlers"
	"github.com/bartosz11/checkmate/api/handlers/auth"
	"github.com/bartosz11/checkmate/api/handlers/checker"
	"github.com/bartosz11/checkmate/api/handlers/heartbeat"
	"github.com/bartosz11/checkmate/api/handlers/incident"
	alarmsIncidents "github.com/bartosz11/checkmate/api/handlers/incident/alarm"
	targetsIncidents "github.com/bartosz11/checkmate/api/handlers/incident/target"
	"github.com/bartosz11/checkmate/api/handlers/notification"
	"github.com/bartosz11/checkmate/api/handlers/orchestrator"
	"github.com/bartosz11/checkmate/api/handlers/session"
	"github.com/bartosz11/checkmate/api/handlers/settings"
	"github.com/bartosz11/checkmate/api/handlers/statuspage"
	"github.com/bartosz11/checkmate/api/handlers/statuspage/domain"
	"github.com/bartosz11/checkmate/api/handlers/statuspage/group"
	"github.com/bartosz11/checkmate/api/handlers/statuspage/notice"
	"github.com/bartosz11/checkmate/api/handlers/statuspage/notice/update"
	statuspageTarget "github.com/bartosz11/checkmate/api/handlers/statuspage/statuspage_target"
	"github.com/bartosz11/checkmate/api/handlers/target"
	"github.com/bartosz11/checkmate/api/handlers/target/alarm"
	"github.com/bartosz11/checkmate/api/handlers/user"
	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/api/middleware"
	"github.com/bartosz11/checkmate/common"
	"github.com/bartosz11/checkmate/common/config"
	_ "github.com/bartosz11/checkmate/docs"
	"github.com/bartosz11/checkmate/frontend"
	"github.com/gin-contrib/cors"
	"github.com/ua-parser/uap-go/uaparser"

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

//go:embed regexes.yaml
var regexes []byte

func StartAPI(db *gorm.DB, router *gin.Engine, production bool, apiConfig *config.APIConfig) {
	if v, ok := binding.Validator.Engine().(*validator.Validate); ok {
		err := v.RegisterValidation("requiredUint", helpers.ValidateRequiredUint)
		if err != nil {
			log.Fatal().Err(err).Msg("failed to register custom uint validator")
		}
	}

	parser, err := uaparser.NewFromBytes(regexes)
	if err != nil {
		log.Fatal().Err(err).Msg("failed to create UA parser")
	}

	if apiConfig.HostDocs {
		router.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))
	}

	if !production {
		router.Use(cors.New(cors.Config{
			AllowOrigins:     []string{"http://localhost:5173", "http://127.0.0.1:5173"},
			AllowMethods:     []string{"PUT", "PATCH", "GET", "OPTIONS", "HEAD", "POST", "DELETE"},
			AllowHeaders:     []string{"Origin", "Content-Type", "Accept", "Authorization"},
			ExposeHeaders:    []string{"Content-Length"},
			AllowCredentials: true,
			MaxAge:           12 * time.Hour,
		}))
	}

	apiGroup := router.Group("/api")
	apiGroup.GET("/health", common.HandleHealthcheck(db))

	helpers.InitJWTHelper(apiConfig)
	helpers.InitHashLimiter(apiConfig.HashingPoolSize)

	authGrp := apiGroup.Group("/auth")
	{
		authGrp.POST("/login", auth.HandleLogin(db, &apiConfig.SecureCookies, parser))
		authGrp.POST("/register", auth.HandleRegister(db))
	}

	publicGroup := apiGroup.Group("", middleware.AuthMiddleware(db, true))
	{
		publicGroup.GET("/target/:targetID", target.HandleGetTargetByID(db))
		publicGroup.GET("/target/:targetID/report", target.HandleGetTargetReportData(db))
		incidentGroup := publicGroup.Group("/incident")
		{
			incidentGroup.GET("/:id", incident.HandleGetIncidentById(db))
			targetGrp := incidentGroup.Group("/target/:id")
			{
				targetGrp.GET("/timerange", targetsIncidents.HandleGetIncidentsForTargetInTimeRange(db))
				targetGrp.GET("/page", targetsIncidents.HandleGetIncidentPageForTarget(db))
			}
			alarmGrp := incidentGroup.Group("/alarm/:id")
			{
				alarmGrp.GET("/last", alarmsIncidents.HandleGetLastIncidentForAlarm(db))
				alarmGrp.GET("/timerange", alarmsIncidents.HandleGetIncidentsForAlarmInTimeRange(db))
				alarmGrp.GET("/page", alarmsIncidents.HandleGetIncidentPageForAlarm(db))
			}
		}
		heartbeatGroup := publicGroup.Group("/heartbeat/:id")
		{
			heartbeatGroup.GET("", heartbeat.HandleGetHeartbeatById(db))
			heartbeatGroup.GET("/last", heartbeat.HandleGetLastHeartbeatForTargetId(db))
			heartbeatGroup.GET("/timerange", heartbeat.HandleGetHeartbeatsForTargetInTimeRange(db))
			heartbeatGroup.GET("/page", heartbeat.HandleGetHeartbeatPageForTarget(db))
		}
	}

	restrictedGrp := apiGroup.Group("", middleware.AuthMiddleware(db, false))
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
				specificTargetGrp.PATCH("", target.HandleUpdateTargetById(db))
				specificTargetGrp.PATCH("/pause", target.HandlePauseTarget(db))
				specificTargetGrp.PATCH("/public", target.HandleTargetTogglePublic(db))
				alarmGrp := specificTargetGrp.Group("/alarm")
				{
					alarmGrp.POST("/", alarm.HandleCreateAlarm(db))
					alarmGrp.GET("/", alarm.HandleGetAllAlarmsByTargetID(db))
					alarmGrp.GET("/:alarmID", alarm.HandleGetAlarmByID(db))
					alarmGrp.DELETE("/:alarmID", alarm.HandleDeleteAlarmByID(db))
					alarmGrp.PATCH("/:alarmID", alarm.HandleUpdateAlarm(db))
					alarmGrp.PATCH("/:alarmID/mute", alarm.HandleMuteAlarm(db))
					alarmGrp.PATCH("/:alarmID/suspend", alarm.HandleSuspendAlarm(db))
				}
				agentGrp := specificTargetGrp.Group("/agent")
				{
					agentGrp.PATCH("/key", target.HandleAgentKeyReset(db))
				}
			}
		}
		checkerGrp := restrictedGrp.Group("/checker")
		{
			checkerGrp.GET("/", checker.HandleGetAllCheckers(db))
			checkerGrp.GET("/:id", checker.HandleGetCheckerByID(db))
			restrictedCheckerGrp := checkerGrp.Group("", middleware.AdminMiddleware())
			{
				restrictedCheckerGrp.POST("/", checker.HandleCreateChecker(db))
				restrictedCheckerGrp.DELETE("/:id", checker.HandleDeleteCheckerByID(db))
				restrictedCheckerGrp.PATCH("/:id", checker.HandleUpdateChecker(db))
				restrictedCheckerGrp.PATCH("/:id/key", checker.HandleRegenCheckerKey(db))
			}
		}
		statuspageGrp := restrictedGrp.Group("/statuspage")
		{
			statuspageGrp.POST("/", statuspage.HandleStatuspageCreate(db))
			statuspageGrp.GET("/", statuspage.HandleGetAllStatuspages(db))
			statuspageGrp.GET("/slug/:slug", statuspage.HandleGetStatuspageBySlug(db))
			specificStatuspageGrp := statuspageGrp.Group("/:statuspageId")
			{
				specificStatuspageGrp.GET("", statuspage.HandleGetStatuspageByID(db))
				specificStatuspageGrp.PATCH("", statuspage.HandleUpdateStatuspageById(db))
				specificStatuspageGrp.DELETE("", statuspage.HandleDeleteStatuspageById(db))
				domainGrp := specificStatuspageGrp.Group("/domain")
				{
					domainGrp.POST("", domain.HandleCreateStatuspageDomain(db))
					domainGrp.GET("", domain.HandleGetAllStatuspageDomains(db))
					specificStatuspageGrp := domainGrp.Group("/:domainId")
					{
						specificStatuspageGrp.GET("", domain.HandleGetStatuspageDomainById(db))
						specificStatuspageGrp.DELETE("", domain.HandleDeleteStatuspageDomainById(db))
						specificStatuspageGrp.PATCH("", domain.HandleUpdateStatuspageDomainById(db))
					}
				}
				groupGrp := specificStatuspageGrp.Group("/group")
				{
					groupGrp.POST("", group.HandleCreateStatuspageGroup(db))
					groupGrp.GET("", group.HandleGetAllStatuspageGroups(db))
					specificStatuspageGrp := groupGrp.Group("/:groupId")
					{
						specificStatuspageGrp.GET("", group.HandleGetStatuspageGroupById(db))
						specificStatuspageGrp.PATCH("", group.HandleUpdateStatuspageGroupById(db))
						specificStatuspageGrp.DELETE("", group.HandleDeleteStatuspageGroupById(db))
					}
				}
				statuspageTargetGrp := specificStatuspageGrp.Group("/target")
				{
					statuspageTargetGrp.POST("", statuspageTarget.HandleCreateStatuspageTarget(db))
					statuspageTargetGrp.GET("", statuspageTarget.HandleGetAllStatuspageTargets(db))
					specificStatuspageTargetGrp := statuspageTargetGrp.Group("/:targetId")
					{
						specificStatuspageTargetGrp.GET("", statuspageTarget.HandleGetStatuspageTargetByIds(db))
						specificStatuspageTargetGrp.PATCH("", statuspageTarget.HandleUpdateStatuspageTargetByIds(db))
						specificStatuspageTargetGrp.DELETE("", statuspageTarget.HandleDeleteStatuspageTargetByIds(db))
					}
				}
				noticeGrp := specificStatuspageGrp.Group("/notice")
				{
					noticeGrp.POST("", notice.HandleCreateStatuspageNotice(db))
					noticeGrp.GET("", notice.HandleGetAllStatuspageNotices(db))
					specificNoticeGrp := noticeGrp.Group("/:noticeId")
					{
						specificNoticeGrp.GET("", notice.HandleGetStatuspageNoticeById(db))
						specificNoticeGrp.DELETE("", notice.HandleDeleteStatuspageNoticeById(db))
						specificNoticeGrp.PATCH("", notice.HandleUpdateStatuspageNoticeById(db))
						updateGrp := specificNoticeGrp.Group("/update")
						{
							updateGrp.POST("", update.HandleCreateStatuspageNoticeUpdate(db))
							updateGrp.GET("", update.HandleGetAllStatuspageNoticeUpdates(db))
							updateGrp.GET("/:updateId", update.HandleGetStatuspageNoticeUpdateById(db))
						}
					}
				}
			}
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
		settingsGrp := adminGrp.Group("/settings")
		{
			//Update endpoint also serves as create endpoint, in this case I think it makes sense
			settingsGrp.POST("/", settings.HandleChangeSetting(db))
			settingsGrp.GET("/", settings.HandleGetAllSettings(db))
			settingsGrp.GET("/:key", settings.HandleGetSettingByKey(db))
			//There will be no delete endpoint - if I ever need to delete settings, it'll be done through migrations in an "if exists" style
		}
	}

	if apiConfig.HostFrontend {
		router.NoRoute(func(c *gin.Context) {
			handlers.StaticFileHandler(c, frontend.FS)
		})
	}
}
