document.getElementById('loginForm').addEventListener('submit', function (event) {
    event.preventDefault();
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    fetch('api/auth/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({email, password}),
    })
        .then(response => response.json())
        .then(data => {
            // 로그인 성공 후 처리
            console.log('Login success:', data);
        })
        .catch((error) => {
            console.error('Login error:', error);
        });
});

document.getElementById('logoutButton').addEventListener('click', function () {
    const accessToken = document.cookie.split('; ').find(row => row.startsWith('POL_ACCESS_TOKEN=')).split('=')[1];

    fetch('api/auth/logout', {
        method: 'POST',
        headers: {
            'Authorization': accessToken,
            'Content-Type': 'application/json'
        },
    })
        .then(response => {
            console.log('Logout success');
        })
        .catch((error) => {
            console.error('Logout error:', error);
        });
});
