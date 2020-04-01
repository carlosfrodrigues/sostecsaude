package mydb

import (
	"database/sql"
	"log"

	// this package needs the _ in the beginning
	_ "github.com/mattn/go-sqlite3"
)

var db *sql.DB

//OpenDB opens the database file
func OpenDB() {
	log.Println("Initializing database...")

	var err error

	db, err = sql.Open("sqlite3", "sostecsaude.sqlite3")

	if err != nil {
		log.Fatal(err.Error())
	}
}

//InitTables creates tables if they do not exists
func InitTables() {
	_, err := db.Exec("pragma foreign_keys=on")

	if err != nil {
		log.Fatal(err.Error())
	}

	queryStr := "create table if not exists usuarios (id integer primary key, perfil text, email text, senha text, "
	queryStr += "unique(email));"

	_, err = db.Exec(queryStr)

	if err != nil {
		log.Fatal(err.Error())
	}

	queryStr = "create table if not exists unidade_saude (id integer primary key, nome text, local text, email text, "
	queryStr += "unique(email));"

	_, err = db.Exec(queryStr)

	if err != nil {
		log.Fatal(err.Error())
	}

	queryStr = "create table if not exists unidade_manutencao (id integer primary key, nome text, setor text, "
	queryStr += "local text, email text, unique(email));"

	_, err = db.Exec(queryStr)

	if err != nil {
		log.Fatal(err.Error())
	}
}

//Close closes the Conn connection
func Close() {
	db.Close()

	log.Println("Conn closed")
}
