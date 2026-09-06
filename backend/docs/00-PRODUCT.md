# Barber SaaS MVP

## Objetivo
Permitir que profissionais que trabalham com agendamento recebam marcações através de um link público.

## Visão Geral
O sistema possui duas áreas distintas:

### Área Pública
- Visualizar serviços disponíveis
- Visualizar horários disponíveis
- Realizar agendamento
- Cancelar agendamento

### Área Administrativa
- Login do profissional
- Visualização da agenda
- Gerenciamento de serviços
- Configuração de horários de funcionamento
- Bloqueio de horários
- Visualização de clientes

## Escopo da V1

### Funcionalidades Incluídas:
- ✅ CRUD de profissionais (barbeiros)
- ✅ CRUD de serviços
- ✅ CRUD de clientes
- ✅ CRUD de agendamentos
- ✅ Configuração de agenda semanal
- ✅ Bloqueio de horários
- Sistema de autenticação simples
- Endpoints públicos para agendamento
- Cancelamento via link/token

### Funcionalidades Excluídas (V2):
- Pagamentos
- Financeiro
- Dashboard
- Estoque
- Fidelidade
- Múltiplos profissionais
- Múltiplas empresas
- Comissões
- Integração com WhatsApp

## Arquitetura Técnica
- Java 17
- Spring Boot 3.5.16
- PostgreSQL
- Arquitetura em camadas: Controller → Service → Repository → Entity → DTO → Mapper
- Módulos existentes: appointments, barbers, barberschedules, customers, scheduleblock, services, weeklyschedule

## Metodologia de Desenvolvimento
- Desenvolvimento orientado a casos de uso
- Cada tarefa representa uma funcionalidade completa utilizável
- Documentação do caso de uso antes da implementação
- Foco em entregar valor incremental ao usuário final

## Próximos Casos de Uso Prioritários
1. Cliente agenda um horário (área pública)
2. Cliente cancela um horário (área pública)
3. Profissional visualiza agenda (área administrativa)
4. Profissional altera horários de funcionamento (área administrativa)
5. Profissional cadastra serviços (área administrativa)

## Regras de Negócio Principais
- Um agendamento deve respeitar os horários de funcionamento
- Não pode haver agendamentos conflitantes
- Cancelamento deve ser possível via link único sem login
- Profissional deve poder bloquear horários indisponíveis

## Status Atual
- ✅ Base técnica implementada
- ✅ CRUDs básicos funcionando
- ✅ Projeto compila sem erros
- ⏳ Funcionalidades de MVP em desenvolvimento