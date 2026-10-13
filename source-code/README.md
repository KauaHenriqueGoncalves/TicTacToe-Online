## TicTacToe - Source Code

### Como executar

1. Crie um arquivo `.env` na raiz desta pasta com o token do ngrok:

```env
NGROK_AUTHTOKEN=seu_token_aqui
```

2. Crie o `.env` da aplicação dentro de `TicTacToeApplication/` (use o `env.example` como base):

```env
HTTP_PORT=8080
HTTP_PREFIX=/api/v1
SOCKET_PORT=8081
ORM_PERSISTENCE=eclipseLinkPersistence
DB_URL=jdbc:h2:./src/main/resources/data/database
DB_USERNAME=admin
DB_PASSWORD=admin
JWT_SECRET=SINGVcB8Uj7zy0MlgXbl9MCIVr3vm
```

3. Suba os containers:

```bash
docker compose up -d
```