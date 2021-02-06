package server

import (
	"log"

	"golang.org/x/net/context"

	firebase "firebase.google.com/go"
	"firebase.google.com/go/messaging"
)

func sendFirebaseMessage(fbToken string, title string, body string, group string) {
	ctx := context.Background()

	app, err := firebase.NewApp(ctx, nil)

	if err != nil {
		log.Println("error initializing firebase: ", err)
	}

	client, err := app.Messaging(ctx)

	if err != nil {
		log.Println("error initializing firebase: ", err)
	}

	message := &messaging.Message{
		Token: fbToken,
		Data: map[string]string{
			"Title": title,
			"Body":  body,
			"Group": group}}

	response, err := client.Send(ctx, message)

	if err != nil {
		log.Println(err)
	}

	// Response is a message ID string.
	log.Println("Successfully sent message:", response)
}

func sendFirebaseMessageToTopic(topic string, title string, body string, group string) {
	ctx := context.Background()

	app, err := firebase.NewApp(ctx, nil)

	if err != nil {
		log.Println("error initializing firebase: ", err)
	}

	client, err := app.Messaging(ctx)

	if err != nil {
		log.Println("error initializing firebase: ", err)
	}

	message := &messaging.Message{
		Topic: topic,
		Data: map[string]string{
			"Title": title,
			"Body":  body,
			"Group": group}}

	response, err := client.Send(ctx, message)

	if err != nil {
		log.Println(err)
	}

	// Response is a message ID string.
	log.Println("Successfully sent message:", response)
}
