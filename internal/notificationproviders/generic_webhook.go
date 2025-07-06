package notificationproviders

import (
	"bytes"
	"encoding/json"
	"github.com/rs/zerolog/log"
	"net/http"
)

func SendGenericWebhookNotification(payload NotificationPayload, credentials string) {
	jsonBytes, err := json.Marshal(payload)
	if err != nil {
		log.Err(err).Msg("failed to marshal notification payload")
		return
	}
	resp, err := http.Post(credentials, "application/json", bytes.NewBuffer(jsonBytes))
	if err != nil {
		log.Err(err).Msg("failed to send generic webhook notification")
		return
	}
	defer resp.Body.Close()
}
