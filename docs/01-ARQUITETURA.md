# Arquitetura do Sistema

## Objetivo

Definir a arquitetura utilizada no Barber SaaS para garantir organização, facilidade de manutenção e escalabilidade durante o crescimento do projeto.

---

# Arquitetura Geral

O sistema será dividido em três partes principais:

```text
FlutterFlow (Frontend Web)
            │
            │ REST API
            ▼
Spring Boot (Backend)
            │
            ▼
      PostgreSQL
```

Cada camada possui uma responsabilidade específica.

---

# Frontend

Tecnologia:

* FlutterFlow (Web)

Responsabilidades:

* Interface do usuário.
* Consumo da API REST.
* Validação básica dos formulários.
* Experiência do usuário.

O frontend não terá acesso direto ao banco de dados.

---

# Backend

Tecnologia:

* Java 17
* Spring Boot

Responsabilidades:

* Regras de negócio.
* Autenticação.
* Agendamentos.
* Validação dos dados.
* Comunicação com o banco.
* Exposição da API REST.

Toda regra importante ficará concentrada no backend.

---

# Banco de Dados

Tecnologia:

* PostgreSQL

Responsabilidades:

* Persistência dos dados.
* Integridade das informações.
* Relacionamentos entre entidades.

---

# Princípios Arquiteturais

Durante todo o desenvolvimento seguiremos estes princípios:

* Separação de responsabilidades.
* Código limpo.
* Arquitetura simples.
* Crescimento incremental.
* Documentação contínua.
* APIs REST padronizadas.
* Escalabilidade desde o início.
