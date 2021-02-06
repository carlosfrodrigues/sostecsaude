package mydb

import (
	"log"
)

// UnidadeSaude é uma estrutura com dados da unidade de saúde usada para criar um objeto json
type UnidadeSaude struct {
	Nome  string
	Local string
	Email string
}

// UnidadeManutencao é uma estrutura com dados da unidade de manutenção usada para criar um objeto json
type UnidadeManutencao struct {
	Nome     string
	Setor    string
	Local    string
	CNPJ     string
	Telefone string
	Email    string
}

// GetEmails retorna uma lista com todos os emails
func GetEmails() []string {
	queryStr := "select email from usuarios"

	rows, err := db.Query(queryStr)

	if err != nil {
		log.Fatal(err.Error())
	}

	defer rows.Close()

	var emails []string

	for rows.Next() {
		var email string

		err = rows.Scan(&email)

		if err != nil {
			log.Fatal(err.Error())
		}

		emails = append(emails, email)
	}

	return emails
}

//GetSenha pega a senha correspondente ao email dado como argumento
func GetSenha(email string) string {
	queryStr := "select senha from usuarios where email=?"

	row := db.QueryRow(queryStr, email)

	var senha string

	err := row.Scan(&senha)

	if err != nil {
		log.Fatal(err.Error())
	}

	return senha
}

//GetPerfil pega o perfil correspondente ao email dado como argumento
func GetPerfil(email string) string {
	queryStr := "select perfil from usuarios where email=?"

	row := db.QueryRow(queryStr, email)

	var perfil string

	err := row.Scan(&perfil)

	if err != nil {
		log.Fatal(err.Error())
	}

	return perfil
}

//Cadastrar adiciona o usuário no banco de dados
func Cadastrar(perfil string, email string, senha string) {
	queryStr := "insert or ignore into usuarios values (null,?,?,?)"

	_, err := db.Exec(queryStr, perfil, email, senha)

	if err != nil {
		log.Println(err.Error())
	}
}

//AtualizarSenha atualiza a senha do usuário
func AtualizarSenha(email string, novaSenha string) {
	queryStr := "update usuarios set senha=? where email=?"

	_, err := db.Exec(queryStr, novaSenha, email)

	if err != nil {
		log.Fatal(err.Error())
	}
}

//UpdateUnidadeSaude atualiza dados cadastrais das unidade de saúde
func UpdateUnidadeSaude(nome string, local string, email string) {
	queryStr := "update unidade_saude set nome=?,local=? where email=?"

	_, err := db.Exec(queryStr, nome, local, email)

	if err != nil {
		log.Fatal(err.Error())
	}

	queryStr = "update equipamentos set unidade=?,local=? where email=?"

	_, err = db.Exec(queryStr, nome, local, email)

	if err != nil {
		log.Fatal(err.Error())
	}
}

//GetUnidadeSaude pega nome e local da unidade de saúde
func GetUnidadeSaude(email string) (string, string) {
	queryStr := "select nome,local from unidade_saude where email=?"

	row := db.QueryRow(queryStr, email)

	var nome string
	var local string

	err := row.Scan(&nome, &local)

	if err != nil {
		log.Println(err.Error())
	}

	return nome, local
}

//UpdateUnidadeManutencao atualiza dados cadastrais das unidade de manutenção
func UpdateUnidadeManutencao(nome string, setor string, local string, cnpj string, telefone string, email string) {
	queryStr := "update unidade_manutencao set nome=?,setor=?,local=?,cnpj=?,telefone=? where email=?"

	_, err := db.Exec(queryStr, nome, setor, local, cnpj, telefone, email)

	if err != nil {
		log.Fatal(err.Error())
	}
}

//GetUnidadeManutencao pega nome e local da unidade de manutenção
func GetUnidadeManutencao(email string) (string, string, string, string, string) {
	queryStr := "select nome,setor,local,cnpj,telefone from unidade_manutencao where email=?"

	row := db.QueryRow(queryStr, email)

	var nome string
	var setor string
	var local string
	var cnpj string
	var telefone string

	err := row.Scan(&nome, &setor, &local, &cnpj, &telefone)

	if err != nil {
		log.Println(err.Error())
	}

	return nome, setor, local, cnpj, telefone
}

// GetWhitelist retorna uma lista com todos os emails da whitelist
func GetWhitelist() []string {
	queryStr := "select email from whitelist"

	rows, err := db.Query(queryStr)

	if err != nil {
		log.Fatal(err.Error())
	}

	defer rows.Close()

	var emails []string

	for rows.Next() {
		var email string

		err = rows.Scan(&email)

		if err != nil {
			log.Fatal(err.Error())
		}

		emails = append(emails, email)
	}

	return emails
}

//RemoverUsuario remove um usuario da tabela "usuarios". As entradas das outras tabelas são removidas pelas regras
// das foreign keys em init.go
func RemoverUsuario(email string) {
	queryStr := "delete from usuarios where email=?"

	_, err := db.Exec(queryStr, email)

	if err != nil {
		log.Println(err.Error())
	}
}

//AddToWhitelist adiciona um usuario na whitelist
func AddToWhitelist(email string) {
	queryStr := "insert or ignore into whitelist values(null,?)"

	_, err := db.Exec(queryStr, email)

	if err != nil {
		log.Println(err.Error())
	}
}

//RemoveFromWhitelist remove um usuario da whitelist
func RemoveFromWhitelist(email string) {
	queryStr := "delete from whitelist where email=?"

	_, err := db.Exec(queryStr, email)

	if err != nil {
		log.Println(err.Error())
	}
}

//UpdateFBtoken atualiza o token do firebase
func UpdateFBtoken(email string, token string) {
	queryStr := "update fb_tokens set token=? where email=?"

	_, err := db.Exec(queryStr, token, email)

	if err != nil {
		log.Fatal(err.Error())
	}
}

//GetFBtoken pega o token do firebase
func GetFBtoken(email string) string {
	queryStr := "select token from fb_tokens where email=?"

	row := db.QueryRow(queryStr, email)

	var token string

	err := row.Scan(&token)

	if err != nil {
		log.Println(err.Error())
	}

	return token
}

//GetFBtokenUnidadeManutencaoByOfertaID pega o token do firebase da unidade de manutencao usando o id da oferta
func GetFBtokenUnidadeManutencaoByOfertaID(id string) string {
	queryStr := "select token from fb_tokens where email=(select email from interessados_manutencao where id=?)"

	row := db.QueryRow(queryStr, id)

	var token string

	err := row.Scan(&token)

	if err != nil {
		log.Println(err.Error())
	}

	return token
}

//GetFBtokenUnidadeSaudeByOfertaID pega o token do firebase da unidade de manutencao usando o id da oferta
func GetFBtokenUnidadeSaudeByOfertaID(id string) string {
	queryStr := `select token from fb_tokens where email=(select email from equipamentos where 
			id=(select id_equipamento from interessados_manutencao where id=?)
		)
	`

	row := db.QueryRow(queryStr, id)

	var token string

	err := row.Scan(&token)

	if err != nil {
		log.Println(err.Error())
	}

	return token
}

//GetListaUsuarios retorna uma lista com o email de todos os usuarios
func GetListaUsuarios() []string {
	queryStr := "select email from usuarios order by email"

	rows, err := db.Query(queryStr)

	if err != nil {
		log.Fatal(err.Error())
	}

	defer rows.Close()

	var emails []string

	for rows.Next() {
		var email string

		err = rows.Scan(&email)

		if err != nil {
			log.Fatal(err.Error())
		}

		emails = append(emails, email)
	}

	return emails
}

//GetListaUnidadeSaude retorna uma lista com as unidade de saúde
func GetListaUnidadeSaude() []UnidadeSaude {
	queryStr := "select nome,local,email from unidade_saude"

	rows, err := db.Query(queryStr)

	if err != nil {
		log.Fatal(err.Error())
	}

	defer rows.Close()

	var unidades []UnidadeSaude

	for rows.Next() {
		var unidade UnidadeSaude

		err = rows.Scan(&unidade.Nome, &unidade.Local, &unidade.Email)

		if err != nil {
			log.Fatal(err.Error())
		}

		unidades = append(unidades, unidade)
	}

	return unidades
}

//GetListaUnidadeManutencao retorna uma lista com as unidade de saúde
func GetListaUnidadeManutencao() []UnidadeManutencao {
	queryStr := "select nome,setor,local,cnpj,telefone,email from unidade_manutencao"

	rows, err := db.Query(queryStr)

	if err != nil {
		log.Fatal(err.Error())
	}

	defer rows.Close()

	var unidades []UnidadeManutencao

	for rows.Next() {
		var unidade UnidadeManutencao

		err = rows.Scan(&unidade.Nome, &unidade.Setor, &unidade.Local, &unidade.CNPJ, &unidade.Telefone, &unidade.Email)

		if err != nil {
			log.Fatal(err.Error())
		}

		unidades = append(unidades, unidade)
	}

	return unidades
}
