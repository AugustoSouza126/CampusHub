# 🎓 CampusHub

O **CampusHub** é um aplicativo Android desenvolvido para facilitar o acesso dos alunos aos eventos promovidos pela universidade.

A plataforma permite que os estudantes criem uma conta, façam login, visualizem eventos disponíveis, consultem informações detalhadas, realizem inscrições e acompanhem os eventos nos quais estão inscritos.

---

## 📱 Sobre o projeto

O CampusHub foi desenvolvido como um projeto acadêmico para aplicar conceitos de desenvolvimento de aplicativos Android, criação de interfaces, gerenciamento de usuários e navegação entre diferentes telas.

A proposta é centralizar os eventos acadêmicos em um único aplicativo, tornando mais simples para os alunos encontrarem atividades, palestras, workshops e outros eventos universitários.

---

## ✨ Funcionalidades

### 👤 Usuário

- Criar uma conta
- Realizar login
- Logout
- Recuperação de senha
- Visualizar perfil
- Editar informações do perfil
- Visualizar eventos disponíveis

### 📅 Eventos

- Listagem de eventos
- Visualização dos detalhes do evento
- Informações sobre:
  - Data
  - Horário
  - Local
  - Descrição
  - Número de vagas
- Inscrição em eventos
- Cancelamento de inscrição

### 🎟️ Meus Eventos

O usuário pode acessar uma área exclusiva com os eventos nos quais está inscrito.

---

## 🖥️ Telas do aplicativo

O aplicativo possui as seguintes telas:

- Login
- Cadastro
- Recuperação de senha
- Página inicial
- Lista de eventos
- Detalhes do evento
- Meus eventos
- Perfil
- Edição de perfil

---

## 🛠️ Tecnologias utilizadas

O projeto foi desenvolvido utilizando:

- **Kotlin**
- **Android Studio**
- **Android SDK**
- **XML**
- **Material Design**
- **AndroidX**
- **CardView**

### Principais componentes

- `AppCompatActivity`
- `LinearLayout`
- `ScrollView`
- `EditText`
- `TextView`
- `MaterialButton`
- `CardView`
- `Toast`

---

## 📂 Estrutura do projeto

```text
CampusHub/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/
│   │       │       └── example/
│   │       │           └── campushub/
│   │       │               └── MainActivity.kt
│   │       │
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml
│   │       │   ├── drawable/
│   │       │   ├── mipmap/
│   │       │   └── values/
│   │       │
│   │       └── AndroidManifest.xml
│   │
│   └── build.gradle.kts
│
├── gradle/
│
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
