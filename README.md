# Dashboard Java

Aplicação fullstack separada em backend Spring Boot e frontend Angular, preparada para escalar posteriormente para app mobile.

## Estrutura

```
dashboard-java/
├── backend/    # Spring Boot 4.x + H2 + JPA
└── frontend/   # Angular 22.x + SCSS + standalone components
```

## Pré-requisitos

- Java 21
- Node.js 24+
- npm 11+

## Backend

```bash
cd backend
./mvnw clean verify      # Linux / Git Bash
mvnw.cmd clean verify    # Windows CMD
./mvnw spring-boot:run   # iniciar servidor na porta 8080
```

Console H2 disponível em: `http://localhost:8080/h2-console`

## Frontend

```bash
cd frontend
npm install
npm run start      # dev server na porta 4200 com proxy para /api -> :8080
npm run build      # build de produção
npm run test       # testes unitários
```

## Próximos passos

- Criar entidades JPA e repositories no backend.
- Expor endpoints REST sob `/api/**`.
- Criar services e componentes Angular para consumir a API.
- Substituir H2 por PostgreSQL/MySQL quando for para produção.
