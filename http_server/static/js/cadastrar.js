/**
 * Cria um novo cadastro
 * @module cadastrar.js
 */

document.getElementById("form_cadastrar").addEventListener("submit", event => {
    event.preventDefault();

    fetch("cadastrar", {
        method: "POST",
        body: new FormData(event.target) // form object
    })
        .then(response => response.text())
        .then(text => {
            Android.credentials(text);
        });
});
