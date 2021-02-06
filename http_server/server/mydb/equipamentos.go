package mydb

import (
	"log"
	"strconv"
)

// Equipamento é uma estrutura com dados dos equipamentos usadas para criar um objeto json
type Equipamento struct {
	ID          string
	Nome        string
	Fabricante  string
	Modelo      string
	NumeroSerie string
	Quantidade  string
	Defeito     string
	Unidade     string
	Local       string
	Email       string
}

type EquipamentoV2 struct {
	ID          string `json:"id"`
	Nome        string `json:"nome"`
	Fabricante  string `json:"fabricante"`
	Modelo      string `json:"modelo"`
	NumeroSerie string `json:"numeroSerie"`
	Quantidade  string `json:"quantidade"`
	Defeito     string `json:"defeito"`
	Unidade     string `json:"unidade"`
	Local       string `json:"local"`
	Email       string `json:"email"`
}

type Empresa struct {
	ID       int    `json:"id"`
	Nome     string `json:"nome"`
	Setor    string `json:"setor"`
	Local    string `json:"local"`
	CNPJ     string `json:"cnpj"`
	Telefone string `json:"telefone"`
	Email    string `json:"email"`
}

type interessadoValue struct {
	ID        string  `json:"id"`
	Estado    int     `json:"estado"`
	UpdatedAt int     `json:"updatedAt"`
	Empresa   Empresa `json:"empresa"`
}

//EstadoEquipamento estrutura usada para enviar para a unidade de saúde um json com o status de manutenção
type EstadoEquipamento struct {
	ID     string `json:"id"`
	Estado int    `json:"estado"`
}

func abs(num int) int {
	if num < 0 {
		return -num
	}
	return num
}

//UnidadeSaudeAdicionarEquipamento adiciona um equipamento com defeito no banco de dados
func UnidadeSaudeAdicionarEquipamento(
	nome string,
	fabricante string,
	modelo string,
	numeroSerie string,
	quantidade int,
	defeito string,
	unidade string,
	local string,
	email string) {
	queryStr := "insert or ignore into equipamentos values (null,?,?,?,?,?,?,?,?,?)"

	_, err := db.Exec(queryStr, nome, fabricante, modelo, numeroSerie, quantidade, defeito, unidade, local, email)

	if err != nil {
		log.Println(err.Error())
	}
}

//UnidadeSaudeAtualizarEquipamento atualiza um equipamento no banco de dados
func UnidadeSaudeAtualizarEquipamento(
	id string,
	nome string,
	fabricante string,
	modelo string,
	numeroSerie string,
	quantidade string,
	defeito string) {
	queryStr := "update equipamentos set nome=?,fabricante=?,modelo=?,numero_serie=?,quantidade=?,defeito=? where id=?"

	_, err := db.Exec(queryStr, nome, fabricante, modelo, numeroSerie, quantidade, defeito, id)

	if err != nil {
		log.Println(err.Error())
	}
}

//UnidadeSaudeRemoverEquipamento remove um equipamento do banco de dados
func UnidadeSaudeRemoverEquipamento(id string) {
	queryStr := "delete from equipamentos where id=?"

	_, err := db.Exec(queryStr, id)

	if err != nil {
		log.Println(err.Error())
	}
}

//GetEstadoEquipamentos pega o status de manutenção dos equipamentos da unidade de saúde dada como argumento
func GetEstadoEquipamentos(emailUnidade string) []EstadoEquipamento {
	queryStr := `select id_equipamento,estado from interessados_manutencao where id_equipamento in 
		(select id from equipamentos where email=?)
	`

	rows, err := db.Query(queryStr, emailUnidade)

	if err != nil {
		log.Println(err.Error())
	}

	defer rows.Close()

	var output []EstadoEquipamento

	for rows.Next() {
		var status EstadoEquipamento

		err = rows.Scan(&status.ID, &status.Estado)

		if err != nil {
			log.Println(err.Error())
		}

		output = append(output, status)
	}

	return output
}

//ListaEquipamentosUnidadeSaude retorna uma lista com os equipamento adicionados pela unidade
func ListaEquipamentosUnidadeSaude(email string) []Equipamento {
	queryStr := `select id,nome,fabricante,modelo,numero_serie,quantidade,defeito,unidade,local from equipamentos 
		where email=? order by nome
	`

	rows, err := db.Query(queryStr, email)

	if err != nil {
		log.Println(err.Error())
	}

	defer rows.Close()

	var equipamentos []Equipamento

	for rows.Next() {
		var equipamento Equipamento

		err = rows.Scan(&equipamento.ID, &equipamento.Nome, &equipamento.Fabricante, &equipamento.Modelo,
			&equipamento.NumeroSerie, &equipamento.Quantidade, &equipamento.Defeito, &equipamento.Unidade,
			&equipamento.Local)

		if err != nil {
			log.Println(err.Error())
		}

		equipamentos = append(equipamentos, equipamento)
	}

	return equipamentos
}

func ListaEquipamentosUnidadeSaudeV2(email string) []EquipamentoV2 {
	// Infelizmente não dá pra typecast direto pro V2 :(
	var equipamentosV2 []EquipamentoV2

	equipamentos := ListaEquipamentosUnidadeSaude(email)

	for _, equipamento := range equipamentos {
		equipamentosV2 = append(equipamentosV2, EquipamentoV2(equipamento))
	}

	return equipamentosV2
}

//ListaTodosEquipamentos retorna uma lista com todos os equipamentos defeituosos
//Esconde equipamentos que já foram aceitos por alguma unidade de manutenção
func ListaTodosEquipamentos() []Equipamento {
	queryStr := `
		select e.id,nome,fabricante,modelo,numero_serie,quantidade,defeito,unidade,local,e.email
		from equipamentos e
		where
		e.email in (select email from whitelist) and
		e.id not in (
			select id_equipamento
			from interessados_manutencao
			where estado > 0)
		order by nome
	`

	rows, err := db.Query(queryStr)

	if err != nil {
		log.Println(err.Error())
	}

	defer rows.Close()

	var equipamentos []Equipamento

	for rows.Next() {
		var equipamento Equipamento

		err = rows.Scan(&equipamento.ID, &equipamento.Nome, &equipamento.Fabricante, &equipamento.Modelo,
			&equipamento.NumeroSerie, &equipamento.Quantidade, &equipamento.Defeito, &equipamento.Unidade,
			&equipamento.Local, &equipamento.Email)

		if err != nil {
			log.Println(err.Error())
		}

		equipamentos = append(equipamentos, equipamento)
	}

	return equipamentos
}

func AlteraEstadoOferta(id string, estadoTo string) (bool, string) {
	var estadoFrom string
	row := db.QueryRow("SELECT estado FROM interessados_manutencao WHERE id=?", id)
	err := row.Scan(&estadoFrom)

	if err != nil {
		log.Println(err.Error())
		return false, err.Error()
	}
	estadoToN, _ := strconv.Atoi(estadoTo)
	estadoFromN, _ := strconv.Atoi(estadoFrom)
	// Cancelar (estado 0) sempre é permitido
	// Só é pertido mudar de um estado para um imediatamente depois
	if estadoTo != "0" && abs(estadoToN-estadoFromN) != 1 {
		return false, "mismatched_state"
	}

	_, err = db.Exec("UPDATE interessados_manutencao SET estado=?, updated_at=strftime('%s', 'now') WHERE id=?", estadoTo, id)

	if err != nil {
		return false, err.Error()
	}
	return true, "ok"
}

//UnidadeManutencaoAdicionarInteresse adiciona na tabela um interesse de realizar manutenção
func UnidadeManutencaoAdicionarInteresse(email string, idEquipamento string) {
	queryStr := "insert or ignore into interessados_manutencao(email,id_equipamento,updated_at) values (?,?,strftime('%s', 'now'))"

	_, err := db.Exec(queryStr, email, idEquipamento)

	if err != nil {
		log.Println(err.Error())
	}
}

//UnidadeManutencaoRemoverInteresse remove da tabela um interesse de realizar manutenção
func UnidadeManutencaoRemoverInteresse(email string, idEquipamento string) {
	queryStr := "delete from interessados_manutencao where email=? and id_equipamento=?"

	_, err := db.Exec(queryStr, email, idEquipamento)

	if err != nil {
		log.Println(err.Error())
	}
}

type infoCliente struct {
	Nome  string `json:"nome"`
	Email string `json:"email"`
}

func GetListaClientes(email string) []infoCliente {
	rows, err := db.Query(`
		select us.nome, us.email
		from interessados_manutencao im
		left join equipamentos e on (e.id = im.id_equipamento)
		left join unidade_saude us on (e.email = us.email)
		where im.email = ?
		group by us.email
	`, email)

	if err != nil {
		log.Println(err.Error())
	}

	var listaClientes []infoCliente
	for rows.Next() {
		var nome string
		var email string

		err = rows.Scan(&nome, &email)
		listaClientes = append(listaClientes, infoCliente{nome, email})
	}
	return listaClientes
}

type equipamentoCliente struct {
	IDOferta  int `json:"idOferta"`
	Estado    int `json:"estado"`
	UpdatedAt int `json:"updatedAt"`
	EquipamentoV2
}

func GetEquipamentosCliente(emailManutencao string, emailSaude string) []equipamentoCliente {
	rows, err := db.Query(`
		select im.id, im.estado, im.updated_at, e.*
		from interessados_manutencao im
		left join equipamentos e on (e.id = im.id_equipamento)
		where
		im.email = ? and
		e.email = ? and
		im.estado > 0
		order by im.estado desc, im.updated_at desc
	`, emailManutencao, emailSaude)

	if err != nil {
		log.Println(err.Error())
	}

	var equipamentos []equipamentoCliente

	for rows.Next() {
		var equipamento equipamentoCliente

		err = rows.Scan(
			&equipamento.IDOferta,
			&equipamento.Estado,
			&equipamento.UpdatedAt,
			&equipamento.ID,
			&equipamento.Nome,
			&equipamento.Fabricante,
			&equipamento.Modelo,
			&equipamento.NumeroSerie,
			&equipamento.Quantidade,
			&equipamento.Defeito,
			&equipamento.Unidade,
			&equipamento.Local,
			&equipamento.Email,
		)

		if err != nil {
			log.Println(err.Error())
		}

		equipamentos = append(equipamentos, equipamento)
	}
	return equipamentos
}

//ListaInteresseManutencao retorna uma lista com os equipamentos que a unidade tem interesse de consertar
func ListaInteresseManutencao(email string) []string {
	queryStr := "select id_equipamento from interessados_manutencao where email=?"

	rows, err := db.Query(queryStr, email)

	if err != nil {
		log.Println(err.Error())
	}

	defer rows.Close()

	var idNumbers []string

	for rows.Next() {
		var idNumber string

		err = rows.Scan(&idNumber)

		if err != nil {
			log.Println(err.Error())
		}

		idNumbers = append(idNumbers, idNumber)
	}

	return idNumbers
}

//ListaInteressadosManutencao retorna uma lista com as unidades interessadas em consertar o equipamento
func ListaInteressadosManutencao(id string) []string {
	queryStr := `select email from interessados_manutencao where id_equipamento=? and 
		email in (select email from whitelist)
	`

	rows, err := db.Query(queryStr, id)

	if err != nil {
		log.Println(err.Error())
	}

	defer rows.Close()

	var emails []string

	for rows.Next() {
		var email string

		err = rows.Scan(&email)

		if err != nil {
			log.Println(err.Error())
		}

		emails = append(emails, email)
	}

	return emails
}

//ListaInteressadosManutencaoV2 retorna também o estado da oferta
//Esconde ofertas para um equipamento que já está sendo consertado
func ListaInteressadosManutencaoV2(id string) []interessadoValue {
	queryStr := `
		select im.id interesseId, im.estado, ifnull(im.updated_at, 0), um.*
		from interessados_manutencao im
		left join unidade_manutencao um using (email)
		where id_equipamento=? and
		(estado > 0 or id_equipamento not in (
			select id_equipamento
			from interessados_manutencao
			where id_equipamento=? and
			estado > 0
		)) and
		um.email in (select email from whitelist)
	`

	rows, err := db.Query(queryStr, id, id)

	if err != nil {
		log.Println(err.Error())
	}

	defer rows.Close()

	var result []interessadoValue

	var interesseID string
	var estado int
	var updatedAt int
	var empresaID int
	var nome string
	var setor string
	var local string
	var cnpj string
	var telefone string
	var email string

	for rows.Next() {
		err = rows.Scan(&interesseID, &estado, &updatedAt, &empresaID, &nome, &setor, &local, &cnpj, &telefone, &email)
		result = append(result, interessadoValue{
			interesseID,
			estado,
			updatedAt,
			Empresa{
				empresaID,
				nome,
				setor,
				local,
				cnpj,
				telefone,
				email,
			},
		})
	}
	return result
}

//GetEquipamentoByID pega equipamento com o id dado
func GetEquipamentoByID(id string) Equipamento {
	queryStr := `select id,nome,fabricante,modelo,numero_serie,quantidade,defeito,unidade,local,email from equipamentos 
		where id=?
	`

	row := db.QueryRow(queryStr, id)

	var equipamento Equipamento

	err := row.Scan(&equipamento.ID, &equipamento.Nome, &equipamento.Fabricante, &equipamento.Modelo,
		&equipamento.NumeroSerie, &equipamento.Quantidade, &equipamento.Defeito, &equipamento.Unidade,
		&equipamento.Local, &equipamento.Email)

	if err != nil {
		log.Println(err.Error())
	}

	return equipamento
}
