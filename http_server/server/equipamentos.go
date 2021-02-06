package server

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"strconv"
	"wwmm/sostecsaude/server/mydb"
)

func unidadeSaudeAdicionarEquipamento(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, _ := verifyToken(w, r)

	if perfil != perfilUnidadeSaude {
		fmt.Fprintf(w, "perfil_invalido")

		return
	}

	if status {
		err := r.ParseForm()

		if err != nil {
			log.Println(logTag + err.Error())
		}

		nome := r.FormValue("nome")
		fabricante := r.FormValue("fabricante")
		modelo := r.FormValue("modelo")
		numeroSerie := r.FormValue("numero_serie")
		quantidade := r.FormValue("quantidade")
		defeito := r.FormValue("defeito")
		unidade := r.FormValue("unidade")
		local := r.FormValue("local")

		n, _ := strconv.Atoi(quantidade)

		mydb.UnidadeSaudeAdicionarEquipamento(nome, fabricante, modelo, numeroSerie, n, defeito, unidade, local, email)

		fmt.Fprintf(w, "Dados inseridos!")

		if inTheWhitelist(email) {
			body := "Equipamento: " + nome + "Unidade: " + unidade + "Local: " + local

			sendFirebaseMessageToTopic(messageTopicPedidoReparo, "Pedido de reparo", body, messageTopicPedidoReparo)
		}
	}
}

func unidadeSaudeAtualizarEquipamento(w http.ResponseWriter, r *http.Request) {
	status, perfil, _, _ := verifyToken(w, r)

	if perfil != perfilUnidadeSaude {
		fmt.Fprintf(w, "perfil_invalido")

		return
	}

	if status {
		err := r.ParseForm()

		if err != nil {
			log.Println(logTag + err.Error())
		}

		id := r.FormValue("id")
		nome := r.FormValue("nome")
		fabricante := r.FormValue("fabricante")
		modelo := r.FormValue("modelo")
		numeroSerie := r.FormValue("numero_serie")
		quantidade := r.FormValue("quantidade")
		defeito := r.FormValue("defeito")

		mydb.UnidadeSaudeAtualizarEquipamento(id, nome, fabricante, modelo, numeroSerie, quantidade, defeito)

		fmt.Fprintf(w, "Dados inseridos!")
	}
}

func unidadeSaudeRemoverEquipamento(w http.ResponseWriter, r *http.Request) {
	status, perfil, _, _ := verifyToken(w, r)

	if perfil != perfilUnidadeSaude {
		fmt.Fprintf(w, "perfil_invalido")

		return
	}

	if status {
		err := r.ParseForm()

		if err != nil {
			log.Println(logTag + err.Error())
		}

		id := r.FormValue("id")

		mydb.UnidadeSaudeRemoverEquipamento(id)

		fmt.Fprintf(w, "Dados removidos!")
	}
}

func unidadeSaudePegarEquipamentos(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, _ := verifyToken(w, r)

	if perfil != perfilUnidadeSaude {
		fmt.Fprintf(w, "perfil_invalido")

		return
	}

	if status {
		equipamentos := mydb.ListaEquipamentosUnidadeSaude(email)

		if len(equipamentos) == 0 {
			fmt.Fprintf(w, "[]")
			return
		}

		jsonEquipamentos, err := json.Marshal(equipamentos)

		if err != nil {
			log.Println(err.Error())
		}

		// fmt.Fprintf(os.Stdout, "%s", jsonEquipamentos)
		fmt.Fprintf(w, "%s", jsonEquipamentos)
	}
}

func unidadeSaudePegarEquipamentosV2(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, _ := verifyToken(w, r)

	if perfil != perfilUnidadeSaude {
		fmt.Fprintf(w, "perfil_invalido")

		return
	}

	if status {
		equipamentos := mydb.ListaEquipamentosUnidadeSaudeV2(email)

		if len(equipamentos) == 0 {
			fmt.Fprintf(w, "[]")
			return
		}

		jsonEquipamentos, err := json.Marshal(equipamentos)

		if err != nil {
			log.Println(err.Error())
		}

		// fmt.Fprintf(os.Stdout, "%s", jsonEquipamentos)
		fmt.Fprintf(w, "%s", jsonEquipamentos)
	}
}

func getEstadoEquipamentos(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, _ := verifyToken(w, r)

	if perfil != perfilUnidadeSaude {
		fmt.Fprintf(w, "perfil_invalido")

		return
	}

	if status {
		listaStatus := mydb.GetEstadoEquipamentos(email)

		if len(listaStatus) == 0 {
			fmt.Fprintf(w, "[]")
			return
		}

		jsonLista, err := json.Marshal(listaStatus)

		if err != nil {
			log.Println(err.Error())
		}

		// fmt.Fprintf(os.Stdout, "%s", jsonEquipamentos)
		fmt.Fprintf(w, "%s", jsonLista)
	}
}

func adminPegarEquipamentos(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, jasonArray := verifyToken(w, r)

	if perfil != perfilAdministrador || email != cfg.AdminEmail {
		fmt.Fprintf(w, "perfil_invalido")

		return
	}

	if status {
		if len(jasonArray) != 2 {
			return
		}

		emailUnidade := jasonArray[1]

		equipamentos := mydb.ListaEquipamentosUnidadeSaude(emailUnidade)

		if len(equipamentos) == 0 {
			fmt.Fprintf(w, "[]")
			return
		}

		jsonEquipamentos, err := json.Marshal(equipamentos)

		if err != nil {
			log.Println(err.Error())
		}

		// fmt.Fprintf(os.Stdout, "%s", jsonEquipamentos)
		fmt.Fprintf(w, "%s", jsonEquipamentos)
	}
}

func listaTodosEquipamentos(w http.ResponseWriter, r *http.Request) {
	status, _, email, _ := verifyToken(w, r)

	if status {
		equipamentos := mydb.ListaTodosEquipamentos()
		idManutencao := mydb.ListaInteresseManutencao(email)

		if len(idManutencao) == 0 {
			idManutencao = append(idManutencao, "-1") // nenhum id válido pode ser menor que zero
		}

		// Força arrays vazios quando serializar em json
		if len(equipamentos) == 0 {
			equipamentos = []mydb.Equipamento{}
		}
		if len(idManutencao) == 0 {
			idManutencao = []string{}
		}

		js, err := json.Marshal([]interface{}{equipamentos, idManutencao})

		if err != nil {
			log.Println(err.Error())
		}

		// fmt.Fprintf(os.Stdout, "%s", js)
		fmt.Fprintf(w, "%s", js)
	}
}

func alteraEstadoOferta(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, jsonArray := verifyToken(w, r)

	id := jsonArray[1]
	estadoTo := jsonArray[2]
	var tipoPerfil = perfilUnidadeManutencao

	if estadoTo == "0" || estadoTo == "1" || estadoTo == "2" || estadoTo == "9" {
		tipoPerfil = perfilUnidadeSaude
	}
	// Estados 3 e 8 são estados que podem ser definidos por ambos saúde ou manutenção
	if estadoTo != "3" && estadoTo != "8" && perfil != tipoPerfil {
		js, _ := json.Marshal([]interface{}{false, "perfil_invalido"})
		fmt.Fprintf(w, "%s", js)
		return
	}

	if status {
		sucesso, err := mydb.AlteraEstadoOferta(id, estadoTo)
		js, _ := json.Marshal([]interface{}{sucesso, err})
		fmt.Fprintf(w, "%s", js)

		if tipoPerfil == perfilUnidadeSaude {
			nomeUnidadeSaude, _ := mydb.GetUnidadeSaude(email)

			fbTokenManutencao := mydb.GetFBtokenUnidadeManutencaoByOfertaID(id)

			var msg string

			switch estadoTo {
			case "1":
				msg = "Oferta de reparo aceita!"
			case "2":
				msg = "Equipamento pronto para ser levado para manutenção!"
			case "9":
				msg = "Equipamento recebido!"
			}

			if len(msg) > 0 {
				sendFirebaseMessage(fbTokenManutencao, nomeUnidadeSaude, msg, nomeUnidadeSaude)
			}
		} else if tipoPerfil == perfilUnidadeManutencao {
			nomeUnidadeManutencao, _, _, _, _ := mydb.GetUnidadeManutencao(email)

			fbTokenSaude := mydb.GetFBtokenUnidadeSaudeByOfertaID(id)

			var msg string

			switch estadoTo {
			case "4":
				msg = "Equipamento recebido pela unidade de manutenção!"
			case "5":
				msg = "Equipamento em triagem!"
			case "6":
				msg = "Equipamento em manutenção!"
			case "7":
				msg = "Equipamento em higienização!"
			case "8":
				msg = "Equipamento saiu para entrega!"
			}

			if len(msg) > 0 {
				sendFirebaseMessage(fbTokenSaude, nomeUnidadeManutencao, msg, nomeUnidadeManutencao)
			}
		}
	}
}

func unidadeManutencaoAtualizarInteresse(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, _ := verifyToken(w, r)

	if perfil != perfilUnidadeManutencao {
		fmt.Fprintf(w, "perfil_invalido")

		return
	}

	if status {
		err := r.ParseForm()

		if err != nil {
			log.Println(logTag + err.Error())
		}

		id := r.FormValue("id")
		state := r.FormValue("state")

		if state == "true" {
			mydb.UnidadeManutencaoAdicionarInteresse(email, id)

			if inTheWhitelist(email) {
				unidadeManutencao, _, _, _, _ := mydb.GetUnidadeManutencao(email)
				equipamento := mydb.GetEquipamentoByID(id)

				fbToken := mydb.GetFBtoken(equipamento.Email)

				sendFirebaseMessage(fbToken, unidadeManutencao, "Interessado em consertar "+equipamento.Nome,
					messageGroupOfertaReparo)
			}
		} else {
			mydb.UnidadeManutencaoRemoverInteresse(email, id)
		}

		fmt.Fprintf(w, "Interesse registrado!")
	}
}

func unidadeManutencaoListaClientes(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, _ := verifyToken(w, r)

	if perfil != perfilUnidadeManutencao {
		fmt.Fprintf(w, "[\"perfil_invalido\"]")
		return
	}
	if !status {
		return
	}

	clientes := mydb.GetListaClientes(email)
	if len(clientes) == 0 {
		fmt.Fprintf(w, "[]")
		return
	}
	js, err := json.Marshal(clientes)
	if err != nil {
		log.Println(err.Error())
	}

	fmt.Fprintf(w, "%s", js)
}

func unidadeManutencaoListaEquipamentosCliente(w http.ResponseWriter, r *http.Request) {
	status, perfil, emailManutencao, jsonArray := verifyToken(w, r)

	if perfil != perfilUnidadeManutencao {
		fmt.Fprintf(w, "[\"perfil_invalido\"]")
		return
	}

	if !status {
		return
	}

	emailSaude := jsonArray[1]

	equipamentos := mydb.GetEquipamentosCliente(emailManutencao, emailSaude)

	if len(equipamentos) == 0 {
		fmt.Fprintf(w, "[]")
		return
	}

	js, err := json.Marshal(equipamentos)

	if err != nil {
		log.Println(err.Error())
	}

	fmt.Fprintf(w, "%s", js)
}

func listaInteressadosManutencao(w http.ResponseWriter, r *http.Request) {
	status, perfil, _, jsonArray := verifyToken(w, r)

	if perfil != perfilUnidadeSaude {
		fmt.Fprintf(w, "perfil_invalido")

		return
	}

	if status {
		type Empresa struct {
			Nome     string
			Setor    string
			Local    string
			CNPJ     string
			Telefone string
			Email    string
		}

		idEquipamento := jsonArray[1]

		emailUnidadesManutencao := mydb.ListaInteressadosManutencao(idEquipamento)

		var empresas []Empresa

		for _, email := range emailUnidadesManutencao {
			empresa := Empresa{}

			empresa.Nome, empresa.Setor, empresa.Local, empresa.CNPJ, empresa.Telefone = mydb.GetUnidadeManutencao(
				email)

			empresa.Email = email

			empresas = append(empresas, empresa)
		}

		if len(empresas) != 0 {
			js, err := json.Marshal(empresas)

			if err != nil {
				log.Println(err.Error())
			}

			// fmt.Fprintf(os.Stdout, "%s", js)
			fmt.Fprintf(w, "%s", js)
		} else {
			js, err := json.Marshal([]string{"empty"})

			if err != nil {
				log.Println(err.Error())
			}

			fmt.Fprintf(w, "%s", js)
		}
	}
}

func listaInteressadosManutencaoV2(w http.ResponseWriter, r *http.Request) {
	status, perfil, _, jsonArray := verifyToken(w, r)

	if perfil != perfilUnidadeSaude {
		fmt.Fprintf(w, "perfil_invalido")

		return
	}

	if status {
		idEquipamento := jsonArray[1]

		interessados := mydb.ListaInteressadosManutencaoV2(idEquipamento)
		if len(interessados) == 0 {
			fmt.Fprintf(w, "[]")
			return
		}

		js, err := json.Marshal(interessados)

		if err != nil {
			log.Println(err.Error())
		}

		// fmt.Fprintf(os.Stdout, "%s", js)
		fmt.Fprintf(w, "%s", js)
	}
}
