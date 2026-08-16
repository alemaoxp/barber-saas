# Splash Screen Design

## Objetivo

Criar a tela inicial do Barber SaaS em Flutter, reproduzindo a composição visual fornecida e executando uma animação sequencial antes da abertura da Home.

## Estrutura

A feature ficará isolada em:

lib/features/splash/presentation/pages/splash_page.dart

A Home ficará em:

lib/features/home/presentation/pages/home_page.dart

Os assets de branding ficarão em:

assets/images/branding/
├── logo.png
├── jhow_cortes.png
└── barbearia.png

## Animação

A tela terá fundo escuro e três elementos independentes:

1. logo.png aparece primeiro.
2. Após 0,5 segundo, jhow_cortes.png aparece.
3. Após mais 0,5 segundo, barbearia.png aparece.

Cada elemento terá uma entrada suave usando fade-in combinado com uma pequena transição vertical.

Após a composição completa permanecer visível por um curto intervalo, o Splash fará fade-out.

Ao final do fade-out, a Home será exibida com fade-in.

## Fluxo

main.dart
↓
SplashPage
↓
logo
↓ 0,5s
Jhow Cortes
↓ 0,5s
Barbearia + slogan
↓
fade-out
↓
HomePage
↓
fade-in

## Responsabilidades

SplashPage será responsável apenas por:

- apresentar a identidade visual;
- controlar a sequência da animação;
- controlar a duração do Splash;
- realizar a transição para a Home.

Não deverá conter regras de negócio, chamadas de API ou lógica de agendamento.

## Assets

Os três PNGs serão tratados como assets independentes para permitir controle individual das animações.

## Compatibilidade

A implementação será feita em Flutter e deverá funcionar no Web/PWA, mantendo a composição centralizada e proporcional em diferentes tamanhos de tela.

## Critério de aceite

Ao abrir o endereço do projeto:

- o Splash aparece primeiro;
- o logo surge;
- 0,5s depois surge "Jhow Cortes";
- 0,5s depois surge "Barbearia" e o slogan;
- a composição faz fade-out;
- a Home entra com fade-in;
- não deve haver tela branca ou salto visual perceptível entre Splash e Home.