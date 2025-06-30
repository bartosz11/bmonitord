package database

import (
	"bmonitord/config"
	"bmonitord/internal/database/model"
	"fmt"
	"github.com/rs/zerolog/log"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
)

func InitDatabase(dbConfig *config.DatabaseConfig) *gorm.DB {
	MigrateDatabase(dbConfig)

	dsn := fmt.Sprintf("host=%s user=%s password=%s dbname=%s port=%d sslmode=%s TimeZone=%s", dbConfig.Host, dbConfig.Username, dbConfig.Password, dbConfig.Database, dbConfig.Port, dbConfig.SSLMode, dbConfig.Timezone)
	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{
		//Logger: logger.Default.LogMode(logger.Info),
	})
	if err != nil {
		log.Fatal().Err(err).Msg("Failed to connect to database")
	}

	//May be useful to "let GORM adjust the DB to it's liking" - so adding any indexes, constraints etc. I may have skipped over
	err = db.AutoMigrate(&model.Setting{}, &model.Orchestrator{}, &model.User{}, &model.Session{}, &model.Notification{}, &model.Target{}, model.Alarm{}, &model.Incident{}, &model.Checker{}, &model.Heartbeat{}, &model.TargetHTTPInfo{}, &model.TargetPingInfo{})
	if err != nil {
		log.Fatal().Err(err).Msg("Failed to auto-migrate database")
	}
	log.Info().Msg("DB init finished")
	return db
}
