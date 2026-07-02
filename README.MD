# Stella D'Italia - App Cliente

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)

Aplicativo Android para clientes da pizzaria Stella D'Italia, com fluxo de cardapio, pedidos, carrinho, perfil e recursos de atendimento ao usuario.

O projeto foi desenvolvido em Kotlin e Android nativo, usando Firebase para autenticacao/dados, bibliotecas AndroidX e uma interface focada em compra e acompanhamento de pedidos.

## Screenshots

| | | |
| --- | --- | --- |
| ![Tela 1](docs/screenshots/screen-01.png) | ![Tela 2](docs/screenshots/screen-02.png) | ![Tela 3](docs/screenshots/screen-03.png) |
| ![Tela 4](docs/screenshots/screen-04.png) | ![Tela 5](docs/screenshots/screen-05.png) | ![Tela 6](docs/screenshots/screen-06.png) |

## Funcionalidades

- Login e cadastro de cliente.
- Cardapio com produtos da pizzaria.
- Carrinho e fluxo de pedido.
- Perfil do usuario.
- Integracao com Firebase.
- Recursos de localizacao, mapas e imagens.

## Tecnologias

- Kotlin
- Android Views/XML
- ViewBinding
- Firebase Auth
- Firebase Realtime Database
- Cloud Firestore
- Firebase Storage
- Firebase Functions
- Room
- Retrofit
- Glide e Picasso
- Google Maps e Location Services
- ZXing para leitura de codigo

## Como executar

### Pre-requisitos

- Android Studio
- JDK 21
- Projeto Firebase configurado

### Build local

```bash
git clone https://github.com/Crofolt/Stella-Cliente.git
cd Stella-Cliente
./gradlew assembleDebug
```

No Windows:

```powershell
.\gradlew.bat assembleDebug
```

## Configuracao local

O app usa Firebase e espera o arquivo local `app/google-services.json`, que nao deve ser versionado por seguranca.

## Autor

Projeto Android do app cliente da Stella D'Italia.
