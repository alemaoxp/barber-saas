# Barber SaaS — Área Administrativa

## 1. Objetivo

A área administrativa é o ambiente utilizado pelo barbeiro para controlar sua operação diária.

Ela faz parte do mesmo Barber SaaS e utiliza:

- o backend Spring Boot existente;
- o frontend Flutter existente;
- as mesmas regras de agenda já implementadas.

Não existe um projeto separado para o administrador.

---

## 2. Tela inicial

A tela inicial da área administrativa será:

# Agenda

Ela deve permitir ao barbeiro entender rapidamente:

- quem está agendado;
- em qual horário;
- quais horários estão livres;
- quais horários estão bloqueados;
- quais serviços serão realizados;
- valor do agendamento.

A prioridade da interface é leitura rápida e baixo ruído visual.

---

## 3. Navegação principal

A navegação inferior inicialmente seguirá:

- Agenda
- Serviços
- Clientes
- Mais

A Agenda será a tela inicial.

As demais áreas serão implementadas progressivamente.

---

## 4. Agenda do dia

A tela apresenta uma data por vez.

No topo:

- título "Agenda";
- dia da semana;
- data selecionada;
- navegação entre dias;
- seletor horizontal de datas;
- possibilidade de abrir calendário completo.

A agenda será dividida visualmente entre:

- Manhã
- Tarde

---

## 5. Estados dos horários

A grade administrativa possui três estados principais:

### FREE

Horário disponível.

Deve possuir baixo destaque visual.

Exemplo:

`09:30   Horário livre   +`

O horário livre não precisa de um grande card chamativo.

---

### OCCUPIED

Horário com agendamento.

Pode apresentar diretamente na agenda:

- horário;
- nome do cliente;
- serviços;
- valor total;
- indicação discreta de "Agendado".

Exemplo:

`10:10   Gabriel Silva`
`        Corte + Barba       R$ 65,00`

Ao tocar no agendamento, abrir a página de detalhes.

---

### BLOCKED

Horário indisponível devido a um ScheduleBlock.

Na área administrativa o bloqueio deve permanecer visível.

Pode apresentar:

- horário;
- indicação de bloqueio;
- motivo, quando existir.

Exemplo:

`14:00   🔒 Bloqueado`
`        Dentista`

Na área pública do cliente esses horários não aparecem como disponíveis.

---

## 6. Detalhes do agendamento

Ao tocar em um horário OCCUPIED, abrir uma página completa.

Essa página poderá apresentar:

- cliente;
- telefone;
- data;
- horário;
- serviços;
- valores individuais;
- valor total;
- status;
- observações.

Ações administrativas como editar ou cancelar poderão existir nessa página.

A Agenda principal não deve exibir todas essas informações para evitar poluição visual.

---

## 7. Seleção de data

Ao tocar na data/calendário, abrir um modal ou bottom sheet sobre a Agenda.

Esse componente apresenta:

- mês atual;
- calendário;
- data selecionada;
- navegação entre meses.

Ao selecionar outra data, a Agenda deve carregar os dados correspondentes.

Não é necessária uma página independente apenas para seleção de data.

---

## 8. Horário livre

Ao tocar em um horário FREE ou no botão `+`, poderá ser exibida uma ação rápida.

Exemplo:

- horário selecionado;
- indicação "Horário livre";
- ação "Novo agendamento".

Não é necessária uma página completa apenas para informar que o horário está livre.

---

## 9. Horário bloqueado

Ao tocar em um horário BLOCKED, poderá ser aberto um modal/bottom sheet.

Ele pode apresentar:

- início do bloqueio;
- fim do bloqueio;
- motivo;
- futuramente ação para editar o bloqueio.

O bloqueio continua pertencendo ao ScheduleBlock existente.

---

## 10. Contrato da Agenda do Dia

O frontend não deve reconstruir a grade da agenda.

O backend deve fornecer uma consulta administrativa consolidada.

Contrato planejado:

`GET /api/v1/barbers/{barberId}/daily-agenda?date=YYYY-MM-DD`

O backend deverá utilizar as regras existentes de:

- WeeklySchedule;
- almoço;
- ScheduleBlock;
- agendamentos;
- cadência fixa.

O retorno deve permitir representar pelo menos:

- FREE;
- OCCUPIED;
- BLOCKED.

`AvailableSlot` não participa dessa grade.

---

## 11. Organização do frontend

A área administrativa deve ser criada progressivamente dentro de:

`frontend/lib/features/admin/`

A primeira feature será a Agenda.

Estrutura inicial sugerida:

`frontend/lib/features/admin/daily_agenda/`

A implementação pode possuir:

- página;
- widgets;
- modelos necessários;
- integração com API.

Não reorganizar todo o frontend público existente apenas para acomodar o admin.

---

## 12. Regra de serviços

Serviços não determinam a duração ou disponibilidade do slot.

A seleção de múltiplos serviços altera somente:

- serviços associados;
- preço total.

A Agenda administrativa deve respeitar a grade fixa existente.

---

## 13. Referência visual aprovada

A direção visual aprovada para a área administrativa possui:

- fundo claro;
- aparência limpa e mobile-first;
- azul escuro como cor principal;
- cards discretos;
- horários livres com pouco destaque;
- agendamentos com destaque moderado;
- bloqueios visualmente diferenciados;
- navegação inferior;
- seletor horizontal de datas;
- detalhes importantes separados da Agenda principal.

A Agenda deve privilegiar leitura rápida.

O barbeiro deve conseguir identificar rapidamente:

1. quem será atendido;
2. em qual horário;
3. quais espaços estão livres;
4. quais períodos estão bloqueados.

Esta referência será utilizada como base visual para as próximas telas administrativas.