package server

import (
	"log"
	"net/smtp"
)

func sendEmailToOurselves(subject string, body string) {
	from := cfg.EmailServer

	// Receiver email address.
	to := []string{cfg.EmailServer}

	auth := smtp.PlainAuth("", from, cfg.EmailServerPassword, "smtp.gmail.com")

	msg := "From: " + cfg.EmailServer + "To: " + cfg.EmailServer + "\nSubject: " + subject + "\n" + body

	err := smtp.SendMail("smtp.gmail.com:587", auth, from, to, []byte(msg))

	if err != nil {
		log.Println(logTag + err.Error())

		return
	}

	log.Println(logTag + "sent email to: " + cfg.EmailServer)
}
