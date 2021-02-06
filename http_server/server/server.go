package server

import (
	"log"
	"net/http"
	"wwmm/sostecsaude/server/mydb"
)

var logTag = "server: "

const (
	perfilUnidadeSaude         = "unidade_saude"
	perfilUnidadeManutencao    = "unidade_manutencao"
	perfilAdministrador        = "administrador"
	messageTopicAdministration = "administration"
	messageTopicPedidoReparo   = "pedido_reparo"
	messageGroupPedidoReparo   = "group_pedido_reparo"
	messageGroupOfertaReparo   = "group_oferta_reparo"
	messageGroupCadastro       = "group_cadastro"
	messageGroupAdministration = "group_administration"
)

// Start http and websockets server
func Start() {
	InitConfig()

	mydb.OpenDB()

	log.Println("Starting server...")

	http.Handle("/", http.FileServer(http.Dir("static")))

	http.HandleFunc("/login", login)
	http.HandleFunc("/cadastrar", cadastrar)
	http.HandleFunc("/alterar_senha", alterarSenha)
	http.HandleFunc("/check_credentials", checkCredentials)
	http.HandleFunc("/check_write_permission", checkWritePermission)
	http.HandleFunc("/update_fb_token", updateFBtoken)
	http.HandleFunc("/update_whitelist", updateWhitelist)
	http.HandleFunc("/remover_usuario", removerUsuario)

	http.HandleFunc("/update_unidade", updateUnidade)
	http.HandleFunc("/get_unidade", getUnidade)

	http.HandleFunc("/unidade_saude_adicionar_equipamento", unidadeSaudeAdicionarEquipamento)
	http.HandleFunc("/unidade_saude_atualizar_equipamento", unidadeSaudeAtualizarEquipamento)
	http.HandleFunc("/unidade_saude_remover_equipamento", unidadeSaudeRemoverEquipamento)
	http.HandleFunc("/unidade_saude_pegar_equipamentos", unidadeSaudePegarEquipamentos)
	http.HandleFunc("/get_estado_equipamentos", getEstadoEquipamentos)
	http.HandleFunc("/v2/unidade_saude_pegar_equipamentos", unidadeSaudePegarEquipamentosV2)
	http.HandleFunc("/v2/altera_estado_oferta", alteraEstadoOferta)

	http.HandleFunc("/admin_pegar_equipamentos", adminPegarEquipamentos)

	http.HandleFunc("/lista_todos_equipamentos", listaTodosEquipamentos)
	http.HandleFunc("/lista_unidade_saude", listaUnidadeSaude)
	http.HandleFunc("/lista_unidade_manutencao", listaUnidadeManutencao)
	http.HandleFunc("/lista_usuarios", listaUsuarios)

	http.HandleFunc("/unidade_manutencao_atualizar_interesse", unidadeManutencaoAtualizarInteresse)
	http.HandleFunc("/unidade_manutencao_lista_clientes", unidadeManutencaoListaClientes)
	http.HandleFunc("/lista_equipamentos_cliente", unidadeManutencaoListaEquipamentosCliente)

	http.HandleFunc("/lista_interessados_manutencao", listaInteressadosManutencao)
	http.HandleFunc("/v2/lista_interessados_manutencao", listaInteressadosManutencaoV2)

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
