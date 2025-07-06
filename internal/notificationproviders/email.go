package notificationproviders

import (
	"bmonitord/internal/orchestrator/helpers"
	"gopkg.in/gomail.v2"
)

func SendEmailNotification(payload NotificationPayload, credentials string) {
	msg := gomail.NewMessage()
	msg.SetHeader("To", credentials)
	msg.SetHeader("Subject", payload.Header)
	msg.SetBody("text/plain", payload.Body)
	helpers.SendEmailMessage(msg)
}
