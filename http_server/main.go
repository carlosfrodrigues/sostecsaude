package main

import (
	"log"
	"os"
	"os/signal"
	"wwmm/sostecsaude/server"
)

var logTag = "main: "

func main() {
	// to change the flags on the default logger
	log.SetFlags(log.LstdFlags | log.Lshortfile)

	c := make(chan os.Signal, 1)

	signal.Notify(c, os.Interrupt)

	go func() {
		<-c

		server.Clean()

		log.Println("Stopping server...")

		os.Exit(1)
	}()

	server.Start()
}
