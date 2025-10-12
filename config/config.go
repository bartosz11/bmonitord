package config

import (
	"strings"

	"github.com/rs/zerolog/log"
	"github.com/spf13/viper"
)

type Config struct {
	DatabaseConfig     DatabaseConfig     `mapstructure:"db"`
	EmailConfig        EmailConfig        `mapstructure:"email"`
	OrchestratorConfig OrchestratorConfig `mapstructure:"orchestrator"`
	APIConfig          APIConfig          `mapstructure:"api"`
	// TODO: consider moving checker config out of the main config
	CheckerConfig CheckerConfig `mapstructure:"checker"`
	LoggingLevel  int8          `mapstructure:"logging-level"`
	PrettyLogging bool          `mapstructure:"pretty-logging"`
	Production    bool          `mapstructure:"production"`
}

type DatabaseConfig struct {
	Host     string `mapstructure:"host"`
	Port     int    `mapstructure:"port"`
	Username string `mapstructure:"username"`
	Password string `mapstructure:"password"`
	Database string `mapstructure:"database"`
	SSLMode  string `mapstructure:"sslmode"`
	Timezone string `mapstructure:"timezone"`
}

type EmailConfig struct {
	Host     string `mapstructure:"host"`
	Port     int    `mapstructure:"port"`
	Username string `mapstructure:"username"`
	Password string `mapstructure:"password"`
	SSL      bool   `mapstructure:"ssl"`
	From     string `mapstructure:"from"`
}

type OrchestratorConfig struct {
	MaxNetworkOverhead int    `mapstructure:"max-network-overhead"`
	Name               string `mapstructure:"name"`
}

type APIConfig struct {
	JWTSecret     string `mapstructure:"jwt-secret"`
	JWTValidity   int    `mapstructure:"jwt-validity"`
	HostDocs      bool   `mapstructure:"host-docs"`
	HostFrontend  bool   `mapstructure:"host-frontend"`
	SecureCookies bool   `mapstructure:"secure-cookies"`
}

type CheckerConfig struct {
	Orchestrators []string `mapstructure:"orchestrators"`
	Key           string   `mapstructure:"key"`
}

func LoadConfig() Config {
	viper.SetConfigName("config")
	viper.SetConfigType("yaml")

	viper.AddConfigPath(".")

	viper.AutomaticEnv()
	viper.EnvKeyReplacer(strings.NewReplacer(".", "_"))

	if err := viper.ReadInConfig(); err != nil {
		log.Fatal().Err(err).Msg("error reading config")
	}

	var config Config
	if err := viper.Unmarshal(&config); err != nil {
		log.Fatal().Err(err).Msg("error unmarshalling config")
	}
	return config
}
