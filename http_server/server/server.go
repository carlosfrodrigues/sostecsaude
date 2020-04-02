package server

import (
	"log"
	"net/http"
	"wwmm/sostecsaude/server/mydb"
)

var logTag = "server: "

// Start http and websockets server
func Start() {
	InitConfig()

	mydb.OpenDB()
	mydb.InitTables()

	log.Println("Starting server...")

	http.Handle("/", http.FileServer(http.Dir("static")))

	http.HandleFunc("/login", login)
	http.HandleFunc("/cadastrar", cadastrar)
	http.HandleFunc("/check_credentials", checkCredentials)

	http.HandleFunc("/update_unidade", updateUnidade)
	http.HandleFunc("/get_unidade", getUnidade)

	http.HandleFunc("/unidade_saude_adicionar_equipamento", unidadeSaudeAdicionarEquipamento)
	http.HandleFunc("/unidade_saude_atualizar_equipamento", unidadeSaudeAtualizarEquipamento)
	http.HandleFunc("/unidade_saude_remover_equipamento", unidadeSaudeRemoverEquipamento)
	http.HandleFunc("/unidade_saude_pegar_equipamentos", unidadeSaudePegarEquipamentos)

	http.HandleFunc("/pegar_todos_equipamentos", pegarTodosEquipamentos)
	http.HandleFunc("/unidade_manutencao_atualizar_interesse", unidadeManutencaoAtualizarInteresse)

	/*
		Start Server
	*/

	log.Println("Listening on port " + cfg.ServerPort)

	http.ListenAndServe(":"+cfg.ServerPort, nil)
}

//Clean free resources
func Clean() {
	mydb.Close()
}
