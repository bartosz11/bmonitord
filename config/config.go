package config

import (
	"github.com/rs/zerolog/log"
	"github.com/spf13/viper"
	"strings"
)

type Config struct {
	OrchestratorConfig struct {
		Database struct {
			Host     string `mapstructure:"host"`
			Port     int    `mapstructure:"port"`
			Username string `mapstructure:"username"`
			Password string `mapstructure:"password"`
			Database string `mapstructure:"database"`
			SSLMode  string `mapstructure:"sslmode"`
			Timezone string `mapstructure:"timezone"`
		} `mapstructure:"db"`
		MaxNetworkOverhead int    `mapstructure:"max-network-overhead"`
		Name               string `mapstructure:"name"`
	} `mapstructure:"orchestrator"`
	PrettyLogging bool `mapstructure:"pretty-logging"`
	LoggingLevel  int8 `mapstructure:"logging-level"`
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
