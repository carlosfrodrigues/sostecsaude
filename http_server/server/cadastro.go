package server

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"time"
	"wwmm/sostecsaude/server/mydb"

	"github.com/dgrijalva/jwt-go"
	"golang.org/x/crypto/bcrypt"
)

//createToken cria Jason Web Token
func createToken(w http.ResponseWriter, perfil string, email string) string {
	claims := jwt.MapClaims{}

	claims["perfil"] = perfil
	claims["email"] = email
	claims["exp"] = time.Now().Add(time.Hour * 24).Unix()

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)

	tokenString, err := token.SignedString([]byte(cfg.TokenSecret))

	if err != nil {
		log.Println("Error: ", err.Error())
	}

	return tokenString
}

func verifyToken(w http.ResponseWriter, r *http.Request) (bool, string, string, []string) {
	var tokenString string
	var perfil string
	var email string
	var jasonArray []string
	var jsonRequest = false

	err := r.ParseForm()

	if err == nil {
		tokenString = r.FormValue("token")
	}

	err = json.NewDecoder(r.Body).Decode(&jasonArray)

	if err == nil {
		jsonRequest = true

		tokenString = jasonArray[0]
	}

	claims := jwt.MapClaims{}

	token, err := jwt.ParseWithClaims(tokenString, claims, func(token *jwt.Token) (interface{}, error) {
		return []byte(cfg.TokenSecret), nil
	})

	if err != nil {
		// log.Println("Error: ", err.Error())

		if !jsonRequest {
			fmt.Fprintf(w, "invalid_token")
		} else {
			response, _ := json.Marshal([]string{"invalid_token"})

			fmt.Fprintf(w, "%s", response)
		}

		return false, perfil, email, jasonArray
	}

	if token.Valid {
		perfil = fmt.Sprintf("%v", claims["perfil"])
		email = fmt.Sprintf("%v", claims["email"])

		if email == cfg.AdminEmail && perfil == perfilAdministrador {
			return true, perfil, email, jasonArray
		}

		emails := mydb.GetEmails()

		for _, dbEmail := range emails {
			if dbEmail == email {
				return true, perfil, email, jasonArray
			}
		}

		if !jsonRequest {
			fmt.Fprintf(w, "invalid_token")
		} else {
			response, _ := json.Marshal([]string{"invalid_token"})

			fmt.Fprintf(w, "%s", response)
		}

		return false, perfil, email, jasonArray
	}

	if !jsonRequest {
		fmt.Fprintf(w, "invalid_token")
	} else {
		response, _ := json.Marshal([]string{"invalid_token"})

		fmt.Fprintf(w, "%s", response)
	}

	return false, perfil, email, jasonArray
}

func checkCredentials(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, _ := verifyToken(w, r)

	if status {
		token := createToken(w, perfil, email) // renovando o token

		fmt.Fprintf(w, token+"<&>"+perfil+"<&>"+email)
	}
}

func cadastrar(w http.ResponseWriter, r *http.Request) {
	err := r.ParseForm()

	if err != nil {
		log.Println(logTag + err.Error())
	}

	senha := r.FormValue("senha")

	email := r.FormValue("email")

	if email == cfg.AdminEmail {
		fmt.Fprintf(w, "invalid_email")

		return
	}

	emails := mydb.GetEmails()

	for _, dbEmail := range emails {
		if dbEmail == email {
			fmt.Fprintf(w, "invalid_email")

			return
		}
	}

	perfil := r.FormValue("perfil")

	hashBytes, err := bcrypt.GenerateFromPassword([]byte(senha), 10)

	if err != nil {
		log.Fatal(logTag + err.Error())
	}

	mydb.Cadastrar(perfil, email, string(hashBytes))

	token := createToken(w, perfil, email)

	fmt.Fprintf(w, token)

	if perfil == perfilUnidadeSaude {
		sendFirebaseMessageToTopic(messageTopicAdministration, "Nova unidade de saúde", "E-mail: "+email,
			messageGroupAdministration)

		//sendEmailToOurselves("Nova unidade de saúde", "E-mail: "+email)
	} else if perfil == perfilUnidadeManutencao {
		sendFirebaseMessageToTopic(messageTopicAdministration, "Nova unidade de manutenção", "E-mail: "+email,
			messageGroupAdministration)

		//sendEmailToOurselves("Nova unidade de manutenção", "E-mail: "+email)
	}
}

// Faz a autenticação do usuário administrador e carrega a página de professor
func login(w http.ResponseWriter, r *http.Request) {
	err := r.ParseForm()

	if err != nil {
		log.Println(logTag + err.Error())
	}

	email, senha := r.FormValue("email"), r.FormValue("senha")

	if isAdministrator(email, senha, w) {
		return
	}

	emails := mydb.GetEmails()

	validEmail := false

	for _, dbEmail := range emails {
		if dbEmail == email {
			validEmail = true

			break
		}
	}

	if !validEmail {
		fmt.Fprintf(w, "Email inválido!")

		return
	}

	hashSenha := mydb.GetSenha(email)

	if bcrypt.CompareHashAndPassword([]byte(hashSenha), []byte(senha)) != nil {
		fmt.Fprintf(w, "Senha inválida!")

		return
	}

	perfil := mydb.GetPerfil(email)

	token := createToken(w, perfil, email) // renovando o token

	fmt.Fprintf(w, token+"<&>"+perfil+"<&>"+email)
}

func updateUnidade(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, _ := verifyToken(w, r)

	if status {
		err := r.ParseForm()

		if err != nil {
			log.Println(logTag + err.Error())
		}

		if perfil == perfilUnidadeSaude {
			nome, local := r.FormValue("nome"), r.FormValue("local")

			mydb.UpdateUnidadeSaude(nome, local, email)
		} else if perfil == perfilUnidadeManutencao {
			nome, setor, local := r.FormValue("nome"), r.FormValue("setor"), r.FormValue("local")

			cnpj, telefone := r.FormValue("cnpj"), r.FormValue("telefone")

			mydb.UpdateUnidadeManutencao(nome, setor, local, cnpj, telefone, email)
		}

		fmt.Fprintf(w, "Dados atualizados!")
	}
}

func getUnidade(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, _ := verifyToken(w, r)

	if status {
		err := r.ParseForm()

		if err != nil {
			log.Println(logTag + err.Error())
		}

		if perfil == perfilUnidadeSaude {
			nome, local := mydb.GetUnidadeSaude(email)

			fmt.Fprintf(w, nome+"<&>"+local)
		} else if perfil == perfilUnidadeManutencao {
			nome, setor, local, cnpj, telefone := mydb.GetUnidadeManutencao(email)

			fmt.Fprintf(w, nome+"<&>"+setor+"<&>"+local+"<&>"+cnpj+"<&>"+telefone)
		}
	}
}

//inTheWhitelist verifica se o usário está na whitelist
func inTheWhitelist(email string) bool {
	emails := mydb.GetWhitelist()

	for _, dbEmail := range emails {
		if email == dbEmail {
			return true
		}
	}

	return false
}

func checkWritePermission(w http.ResponseWriter, r *http.Request) {
	status, _, email, _ := verifyToken(w, r)

	if status {
		if inTheWhitelist(email) {
			fmt.Fprintf(w, "has_write_permission")
		} else {
			fmt.Fprintf(w, "no_write_permission")
		}
	}
}

func updateFBtoken(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, _ := verifyToken(w, r)

	if status {
		if perfil == perfilAdministrador || email == cfg.AdminEmail {
			return
		}

		err := r.ParseForm()

		if err != nil {
			log.Println(logTag + err.Error())
		}

		mydb.UpdateFBtoken(email, r.FormValue("fb_token"))
	}
}

func isAdministrator(email string, senha string, w http.ResponseWriter) bool {
	if email == cfg.AdminEmail {
		if senha == cfg.AdminPassword {
			token := createToken(w, perfilAdministrador, email) // renovando o token

			fmt.Fprintf(w, token+"<&>"+perfilAdministrador+"<&>"+email)
		} else {
			fmt.Fprintf(w, "Senha inválida!")
		}

		return true
	}

	return false
}

func listaUsuarios(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, _ := verifyToken(w, r)

	if status {
		if perfil == perfilAdministrador && email == cfg.AdminEmail {
			emails := mydb.GetListaUsuarios()

			js, err := json.Marshal(emails)

			if err != nil {
				log.Println(err.Error())
			}

			// fmt.Fprintf(os.Stdout, "%s", js)
			fmt.Fprintf(w, "%s", js)
		}
	}
}

func listaUnidadeSaude(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, _ := verifyToken(w, r)

	if status {
		if perfil == perfilAdministrador && email == cfg.AdminEmail {
			unidades := mydb.GetListaUnidadeSaude()
			whitelist := mydb.GetWhitelist()

			if len(whitelist) == 0 {
				whitelist = append(whitelist, "")
			}

			js, err := json.Marshal([]interface{}{unidades, whitelist})

			if err != nil {
				log.Println(err.Error())
			}

			// fmt.Fprintf(os.Stdout, "%s", js)
			fmt.Fprintf(w, "%s", js)
		}
	}
}

func listaUnidadeManutencao(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, _ := verifyToken(w, r)

	if status {
		if perfil == perfilAdministrador && email == cfg.AdminEmail {
			unidades := mydb.GetListaUnidadeManutencao()
			whitelist := mydb.GetWhitelist()

			if len(whitelist) == 0 {
				whitelist = append(whitelist, "")
			}

			js, err := json.Marshal([]interface{}{unidades, whitelist})

			if err != nil {
				log.Println(err.Error())
			}

			// fmt.Fprintf(os.Stdout, "%s", js)
			fmt.Fprintf(w, "%s", js)
		}
	}
}

func updateWhitelist(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, _ := verifyToken(w, r)

	if status {
		if perfil == perfilAdministrador && email == cfg.AdminEmail {
			err := r.ParseForm()

			if err != nil {
				log.Println(logTag + err.Error())
			}

			emailUnidade := r.FormValue("email")
			state := r.FormValue("state")

			if state == "true" {
				mydb.AddToWhitelist(emailUnidade)

				fbToken := mydb.GetFBtoken(emailUnidade)

				sendFirebaseMessage(fbToken, "Validação de Cadastro", "Seu cadastro foi validado!",
					messageGroupCadastro)
			} else {
				mydb.RemoveFromWhitelist(emailUnidade)
			}

			fmt.Fprintf(w, "Operação realizada!")
		}
	}
}

func removerUsuario(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, _ := verifyToken(w, r)

	if status {
		if perfil == perfilAdministrador && email == cfg.AdminEmail {
			err := r.ParseForm()

			if err != nil {
				log.Println(logTag + err.Error())
			}

			emailUnidade := r.FormValue("email")

			mydb.RemoverUsuario(emailUnidade)

			fmt.Fprintf(w, "Operação realizada!")
		}
	}
}

func alterarSenha(w http.ResponseWriter, r *http.Request) {
	status, perfil, email, _ := verifyToken(w, r)

	if status {
		err := r.ParseForm()

		if err != nil {
			log.Println(logTag + err.Error())
		}

		if perfil == perfilAdministrador {
			emailUsuario := r.FormValue("email_usuario")

			novaSenha := r.FormValue("nova_senha")

			if len(novaSenha) < 6 {
				fmt.Fprintf(w, "A senha deve ter pelo menos 6 characteres!")

				return
			}

			hashBytes, err := bcrypt.GenerateFromPassword([]byte(novaSenha), 10)

			if err != nil {
				log.Fatal(logTag + err.Error())
			}

			mydb.AtualizarSenha(emailUsuario, string(hashBytes))

			fmt.Fprintf(w, "Senha alterada!")
		} else {
			senhaAtual := r.FormValue("senha_atual")

			hashSenhaAtual := mydb.GetSenha(email)

			if bcrypt.CompareHashAndPassword([]byte(hashSenhaAtual), []byte(senhaAtual)) != nil {
				fmt.Fprintf(w, "senha_atual_invalida")

				return
			}

			novaSenha := r.FormValue("nova_senha")

			if len(novaSenha) < 6 {
				fmt.Fprintf(w, "A senha deve ter pelo menos 6 characteres!")

				return
			}

			hashBytes, err := bcrypt.GenerateFromPassword([]byte(novaSenha), 10)

			if err != nil {
				log.Fatal(logTag + err.Error())
			}

			mydb.AtualizarSenha(email, string(hashBytes))

			fmt.Fprintf(w, "Senha alterada!")
		}
	}
}
