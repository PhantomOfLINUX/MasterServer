document
    .getElementById("emailForm")
    .addEventListener("submit", function (event) {
        event.preventDefault();

        const email = document.getElementById("email").value;
        const getUrl = `https://api.pol.or.kr/api/signup/email/${email}`;

        fetch(getUrl)
            .then((response) => response.json())
            .then((data) => {
                console.log("GET Success:", data);
            })
            .catch((error) => {
                console.error("GET Error:", error);
            });
    });

document.getElementById("sendEmail").addEventListener("click", function () {
    const email = document.getElementById("email").value;
    const verifyUrl = `https://api.pol.or.kr/api/signup/email/${email}/verify`;

    fetch(verifyUrl)
        .then((response) => response.json())
        .then((data) => {
            console.log("Verification Email Sent:", data);
        })
        .catch((error) => {
            console.error("Error:", error);
        });
});

document.getElementById("getOAuthURL").addEventListener("click", function () {
    const oauthUrl = "https://api.pol.or.kr/api/oauth2/google-url";

    fetch(oauthUrl, {
        headers: {
            Accept: "text/plain",
        },
    })
        .then((response) => response.text())
        .then((data) => {
            console.log("OAuth URL:", data);
        })
        .catch((error) => {
            console.error("Error:", error);
        });
});

document
    .getElementById("loginForm")
    .addEventListener("submit", function (event) {
        event.preventDefault();

        const email = document.getElementById("loginEmail").value;
        const password = document.getElementById("loginPassword").value;
        const loginUrl = "https://api.pol.or.kr/api/login/pol";

        fetch(loginUrl, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ email: email, password: password }),
        })
            .then((response) => response.json())
            .then((data) => {
                console.log("Login Success:", data);
            })
            .catch((error) => {
                console.error("Login Error:", error);
            });
    });
