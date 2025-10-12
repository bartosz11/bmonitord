package database

import (
	"database/sql"
	"embed"
	"fmt"

	"github.com/bartosz11/checkmate/common/config"
	"github.com/golang-migrate/migrate/v4"
	"github.com/golang-migrate/migrate/v4/database/postgres"
	"github.com/golang-migrate/migrate/v4/source/iofs"
	"github.com/rs/zerolog/log"
)

//go:embed migrations/*.sql
var migrations embed.FS

func MigrateDatabase(dbConfig *config.DatabaseConfig) {

	source, err := iofs.New(migrations, "migrations")
	if err != nil {
		log.Fatal().Err(err).Msg("Failed to load migrations")
	}

	url := fmt.Sprintf("postgres://%s:%s@%s:%d/%s?sslmode=%s", dbConfig.Username, dbConfig.Password, dbConfig.Host, dbConfig.Port, dbConfig.Database, dbConfig.SSLMode)
	sqlDB, err := sql.Open("postgres", url)
	if err != nil {
		log.Fatal().Err(err).Msg("Failed to connect to database")
	}

	driver, err := postgres.WithInstance(sqlDB, &postgres.Config{})
	if err != nil {
		log.Fatal().Err(err).Msg("Failed to connect to database")
	}

	instance, err := migrate.NewWithInstance("iofs", source, "postgres", driver)
	if err != nil {
		log.Fatal().Err(err).Msg("Failed to create migration instance")
	}

	err = instance.Up()
	if err != nil && err.Error() != "no change" {
		log.Fatal().Err(err).Msg("Failed to run migrations")
	}
	log.Info().Msg("Successfully migrated database schema with SQL migrations")
}
