package providers

import "github.com/rs/zerolog/log"

func SendDiscordNotification(header string, body string, credentials string) {
	//fixme: just a mock for now
	log.Info().Str("header", header).Str("body", body).Str("credentials", credentials).Msg("New Discord notification")
}
