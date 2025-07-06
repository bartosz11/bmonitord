package notificationproviders

import (
	"fmt"
	"github.com/rs/zerolog/log"
	"net/http"
	"strings"
)

const discordBodyTemplate = "{ \"embeds\": [ { \"color\": 15965440, \"title\": \"%s\", \"description\": \"%s\" } ]}"

func SendDiscordNotification(payload NotificationPayload, credentials string) {
	body := fmt.Sprintf(discordBodyTemplate, payload.Header, payload.Body)
	resp, err := http.Post(credentials, "application/json", strings.NewReader(body))
	if err != nil {
		log.Err(err).Msg("Failed to send Discord notification")
		return
	}
	defer resp.Body.Close()
}
