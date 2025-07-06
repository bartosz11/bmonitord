package notificationproviders

import (
	"fmt"
	"github.com/rs/zerolog/log"
	"net/http"
	"strings"
)

const pushbulletBodyTemplate = "{\"type\":\"note\",\"title\":\"%s\",\"body\":\"%s\"}"

func SendPushBulletNotification(payload NotificationPayload, credentials string) {
	body := fmt.Sprintf(pushbulletBodyTemplate, payload.Header, payload.Body)

	request, err := http.NewRequest(http.MethodPost, "https://api.pushbullet.com/v2/pushes", strings.NewReader(body))
	if err != nil {
		log.Err(err).Msg("error creating request")
		return
	}
	request.Header.Set("Content-Type", "application/json")
	request.Header.Set("Access-Token", credentials)

	resp, err := http.DefaultClient.Do(request)
	if err != nil {
		log.Err(err).Msg("failed to send Pushbullet notification")
		return
	}
	defer resp.Body.Close()
}
