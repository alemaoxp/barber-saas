# Barber SaaS — Agent Rules

Este arquivo contém regras obrigatórias para qualquer agente de IA que trabalhe neste projeto.

## 1. Regra principal

Não alterar, inventar ou reinterpretar regras de negócio sem autorização explícita.

Antes de implementar algo novo, reutilizar a arquitetura e as regras existentes sempre que possível.

---

## 2. Arquitetura do projeto

O projeto possui:

- `backend/`: Java 17 + Spring Boot + PostgreSQL.
- `frontend/`: Flutter Web/PWA.
- `api/`: coleções e testes manuais de API.
- `docs/`: documentação do produto e arquitetura.
- `prompts/`: prompts auxiliares.

A área administrativa do barbeiro faz parte do mesmo backend e do mesmo frontend.

Não criar um terceiro projeto ou pasta raiz `admin/`.

A organização administrativa do frontend deve ficar progressivamente dentro de:

`frontend/lib/features/admin/`

Não reorganizar o frontend público existente apenas para acomodar a área administrativa.

---

## 3. Agenda e horários

Os horários de agendamento são FIXOS.

A disponibilidade NÃO depende da duração dos serviços selecionados.

Selecionar:

- Corte
- Corte + Barba
- Corte + Barba + Sobrancelha

não altera:

- horário disponível;
- cadência da agenda;
- ocupação do slot;
- duração utilizada para disponibilidade.

Múltiplos serviços alteram apenas:

- serviços vinculados ao agendamento;
- preço total.

Nunca somar duração de serviços para calcular disponibilidade.

---

## 4. Cadência atual

Segunda-feira:

- cadência de 40 minutos.

Demais dias de atendimento:

- cadência de 30 minutos.

A grade da tarde reinicia após o almoço.

A lógica existente do backend deve ser reutilizada e não duplicada no frontend.

---

## 5. Fonte de verdade

O backend é a fonte de verdade para:

- expediente;
- horários;
- almoço;
- bloqueios;
- disponibilidade;
- conflitos;
- estado do agendamento.

O frontend deve renderizar os dados recebidos e não reconstruir regras de agenda.

---

## 6. WeeklySchedule e ScheduleBlock

`WeeklySchedule` representa a jornada semanal recorrente.

`ScheduleBlock` representa exceções, como:

- compromissos;
- férias;
- feriados;
- ausência;
- bloqueios manuais.

Na área administrativa, horários bloqueados devem poder aparecer visualmente como `BLOCKED`.

Na área pública do cliente, horários bloqueados simplesmente não devem aparecer como disponíveis.

---

## 7. AvailableSlot

`AvailableSlot` NÃO representa a grade normal da agenda.

Ele existe exclusivamente para o fluxo de antecipação de vaga.

Nunca utilizar `AvailableSlot` para construir a Agenda do Dia administrativa.

---

## 8. Antecipação de vaga

Não existe fila por ordem de criação.

Não existe janela de 10 minutos.

Quando surge uma vaga anterior:

- clientes elegíveis podem ser notificados;
- todos competem pela mesma vaga;
- o primeiro aceite válido fica com a vaga;
- o backend deve garantir isso sob concorrência.

Não alterar essa regra sem autorização.

---

## 9. Área administrativa

A primeira tela administrativa será:

`Agenda do dia`

Ela deverá permitir visualizar a grade diária com estados como:

- `FREE`
- `OCCUPIED`
- `BLOCKED`

Agendamentos ocupados podem exibir:

- cliente;
- serviços;
- valor total;
- horário.

Detalhes adicionais devem ficar em uma tela própria de detalhes do agendamento.

A Agenda do Dia deve priorizar leitura rápida e baixo ruído visual.

---

## 10. Alterações de código

Não alterar arquivos fora do escopo da tarefa sem necessidade real.

Não realizar grandes refatorações oportunistas.

Não adicionar dependências sem necessidade.

Não alterar schema ou banco manualmente sem solicitação.

Não alterar regras para fazer testes passarem.

---

## 11. Git

Não executar:

- `git commit`
- `git push`

a menos que seja explicitamente solicitado.

Não utilizar `git add .` ou `git add -A` automaticamente.

Não apagar arquivos não rastreados sem investigar primeiro.

---

## 12. Validação

Depois de mudanças no backend, executar os testes relevantes.

Preferencialmente:

`./mvnw test`

Depois de mudanças no frontend, executar conforme necessário:

`flutter analyze`

`flutter test`

Se houver alteração relevante para build web:

`flutter build web`

Não afirmar que algo funciona sem verificar quando a verificação estiver disponível.

---

## 13. Relatórios dos agentes

Quando solicitado um relatório detalhado, sobrescrever:

`backend/docs/codex-last-report.md`

No chat do agente, retornar apenas um resumo curto.

Não adicionar `codex-last-report.md` ao Git.

---

## 14. Princípio de implementação

Preferir:

1. reutilizar código existente;
2. pequenas mudanças;
3. contratos claros;
4. backend como fonte de verdade;
5. testes;
6. implementação incremental.

Evitar criar abstrações antecipadas para funcionalidades que ainda não existem.