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

//InitTables creates and initializes tables if they do not exists
func InitTables() {
	// Enable foreign keys

	_, err := db.Exec("pragma foreign_keys=on")

	if err != nil {
		log.Fatal(err.Error())
	}

	// Enable WAL(Write-Ahead Log) para melhorar a performance https://www.sqlite.org/wal.html
	_, err = db.Exec("pragma journal_mode=WAL")

	if err != nil {
		log.Fatal(err.Error())
	}

	// create tables

	queryStr := `create table if not exists usuarios (
		id integer primary key autoincrement,
		perfil text,
		email text unique,
		senha text
	);
	`

	_, err = db.Exec(queryStr)

	if err != nil {
		log.Fatal(err.Error())
	}

	queryStr = `create table if not exists unidade_saude (
		id integer primary key autoincrement,
		nome text,
		local text,
		email text unique,
		foreign key(email) references usuarios(email) on delete cascade
	);
	`
	_, err = db.Exec(queryStr)

	if err != nil {
		log.Fatal(err.Error())
	}

	queryStr = `create table if not exists unidade_manutencao (
		id integer primary key autoincrement, 
		nome text,
		setor text,
		local text,
		cnpj text,
		telefone text,
		email text unique,
		foreign key(email) references usuarios(email) on delete cascade
	);
	`

	_, err = db.Exec(queryStr)

	if err != nil {
		log.Fatal(err.Error())
	}

	queryStr = `create table if not exists equipamentos (
		id integer primary key autoincrement,
		nome text,
		fabricante text,
		modelo text,
		numero_serie text,
		quantidade int,
		defeito text,
		unidade text,
		local text,
		email text,
		foreign key(email) references unidade_saude(email) on delete cascade,
		unique(nome,fabricante,modelo,numero_serie,quantidade,defeito,unidade,local,email)
	);
	`
	_, err = db.Exec(queryStr)

	if err != nil {
		log.Fatal(err.Error())
	}

	queryStr = `create table if not exists interessados_manutencao (
		id integer primary key autoincrement,
		email string,
		id_equipamento integer,
		foreign key(email) references unidade_manutencao(email) on delete cascade,
		foreign key(id_equipamento) references equipamentos(id) on delete cascade,
		unique(email,id_equipamento)
	);
	`

	_, err = db.Exec(queryStr)

	if err != nil {
		log.Fatal(err.Error())
	}

	// create triggers

	queryStr = `create trigger if not exists add_unidade_saude after insert on usuarios
		when new.perfil = "unidade_saude"
		begin
			insert or ignore into unidade_saude values(null,"","",new.email);
		end;		
	`

	_, err = db.Exec(queryStr)

	if err != nil {
		log.Fatal(err.Error())
	}

	queryStr = `create trigger if not exists add_unidade_manutencao after insert on usuarios
		when new.perfil = "unidade_manutencao"
		begin
			insert or ignore into unidade_manutencao values(null,"","","","","",new.email);
		end;		
	`

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
