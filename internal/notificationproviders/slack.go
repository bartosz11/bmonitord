package notificationproviders

import (
	"fmt"
	"github.com/rs/zerolog/log"
	"net/http"
	"strings"
)

const slackBodyTemplate = "{\"text\":\"\",\"blocks\":[{\"type\":\"header\",\"text\":{\"type\":\"plain_text\",\"text\":\"%s\",\"emoji\":true}},{\"type\":\"section\",\"text\":{\"type\":\"mrkdwn\",\"text\":\"%s\"}}]}"

func SendSlackNotification(payload NotificationPayload, credentials string) {
	body := fmt.Sprintf(slackBodyTemplate, payload.Header, payload.Body)
	resp, err := http.Post(credentials, "application/json", strings.NewReader(body))
	if err != nil {
		log.Err(err).Msg("Failed to send Slack notification")
		return
	}
	defer resp.Body.Close()
}
