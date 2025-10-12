package notificationproviders

import (
	"fmt"
	"github.com/rs/zerolog/log"
	"net/http"
	"strings"
)

const gotifyBodyTemplate = "{\"title\":\"%s\",\"message\":\"%s\",\"priority\":8}"

func SendGotifyNotification(payload NotificationPayload, credentials string) {
	body := fmt.Sprintf(gotifyBodyTemplate, payload.Header, payload.Body)
	credentialsSplit := strings.Split(credentials, ";")
	url := credentialsSplit[0] + "/message"
	token := credentialsSplit[1]
	req, err := http.NewRequest("POST", url, strings.NewReader(body))
	if err != nil {
		log.Err(err).Msg("error creating http request for Gotify notification")
		return
	}
	req.Header.Add("X-Gotify-Key", token)
	req.Header.Add("Content-Type", "application/json")
	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		log.Err(err).Msg("error sending Gotify notification")
		return
	}
	defer resp.Body.Close()
}
