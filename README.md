# Prova de Desenvolvimento JSF – Cálculo de IRRF, CRUD e Relatórios

Este projeto é um sistema de folha de pagamento desenvolvido em JSF (JavaServer Faces), com funcionalidades de CRUD (Cadastro, Leitura, Atualização e Exclusão) de funcionários e usuários, cálculo do IRRF (Imposto de Renda Retido na Fonte), e geração de relatórios. Abaixo, você encontrará as instruções detalhadas para configurar o ambiente de desenvolvimento, incluindo Tomcat 10, PostgreSQL e os detalhes sobre as tabelas e funções utilizadas no banco de dados.

## Pré-requisitos

Antes de iniciar a configuração, verifique se os seguintes pré-requisitos estão atendidos:

- **Apache Tomcat 10.x**: Servidor de aplicação para deploy da aplicação web.
- **PostgreSQL 12+**: Banco de dados utilizado para armazenar os dados da aplicação.
- **JDK 11 ou superior**: Necessário para compilar e executar o sistema.

## Configuração do Apache Tomcat 10.x

### Passo 1: Baixar e Instalar o Tomcat

1. Acesse o site oficial do [Apache Tomcat](https://tomcat.apache.org/download-10.cgi) e baixe a versão mais recente do Tomcat 10.x.
2. Extraia o arquivo compactado em um diretório de sua escolha no seu sistema.

### Passo 2: Configuração do Tomcat

1. Navegue até o diretório `conf` do Tomcat e abra o arquivo `server.xml`.
2. Certifique-se de que a porta de conexão do Tomcat (por padrão, `8080`) esteja disponível e não conflite com outras aplicações.
   
### Passo 3: Deploy da Aplicação

1. Coloque o arquivo `.war` do projeto na pasta `webapps` do Tomcat.
2. Inicie o Tomcat executando o script `bin/startup.sh` (Linux/macOS) ou `bin/startup.bat` (Windows).
3. Acesse a aplicação em `http://localhost:8080/`.

## Configuração do Banco de Dados PostgreSQL

### Passo 1: Instalar o PostgreSQL

1. Baixe e instale a versão mais recente do PostgreSQL em [https://www.postgresql.org/download/](https://www.postgresql.org/download/).
2. Após a instalação, configure o banco de dados no seu SGBD (Sistema de Gerenciamento de Banco de Dados), utilizando o usuário `postgres` e senha `1234`.

### Passo 2: Configuração da Conexão no Banco

Crie o banco de dados e configure as tabelas necessárias para o funcionamento do sistema.

#### Comandos SQL para criação das tabelas e função

```sql
-- Tabela de funcionário (mantida sem alterações)
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) UNIQUE NOT NULL,
    base_salary NUMERIC(10,2) NOT NULL
);

-- Tabela de usuário (sem relação com employee)
CREATE TABLE user_account (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- senha criptografada
    email VARCHAR(100) UNIQUE NOT NULL,
    is_admin BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT NOW() NOT NULL
);

-- Tabela de folha de pagamento
CREATE TABLE payroll (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    month_year VARCHAR(7) NOT NULL,
    base_salary NUMERIC(10,2) NOT NULL,
    other_benefits NUMERIC(10,2) DEFAULT 0 NOT NULL,
    discounts NUMERIC(10,2) DEFAULT 0 NOT NULL,
    irrf NUMERIC(10,2) NOT NULL,
    net_value NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW() NOT NULL
);
```

### Passo 3: Função PL/pgSQL para Cálculo do IRRF
Crie a função no banco de dados para calcular o IRRF com base no salário base e outros benefícios:

```sql
-- Função PL/pgSQL para calcular o IRRF
CREATE OR REPLACE FUNCTION calculate_irrf(
    base_salary NUMERIC(10,2), 
    other_benefits NUMERIC(10,2)
) RETURNS NUMERIC(10,2) AS $$
DECLARE
    calculation_base NUMERIC(10,2);
    aliquot NUMERIC(5,2);
    deduction NUMERIC(10,2);
    irrf_value NUMERIC(10,2);
BEGIN
    calculation_base := base_salary + other_benefits;
    
    IF calculation_base <= 1903.98 THEN
        aliquot := 0;
        deduction := 0;
    ELSIF calculation_base <= 2826.65 THEN
        aliquot := 7.5;
        deduction := 142.80;
    ELSIF calculation_base <= 3751.05 THEN
        aliquot := 15;
        deduction := 354.80;
    ELSIF calculation_base <= 4664.68 THEN
        aliquot := 22.5;
        deduction := 636.13;
    ELSE
        aliquot := 27.5;
        deduction := 869.36;
    END IF;
    
    irrf_value := ROUND(GREATEST((calculation_base * (aliquot / 100)) - deduction, 0), 2);

    RETURN irrf_value;
END;
$$ LANGUAGE plpgsql;
```

### Passo 4: Inserção de um Usuário Admin
Execute o seguinte comando para inserir um usuário admin, necessário para acessar a interface de administração da aplicação:

```sql
-- Inserir o usuário administrador
INSERT INTO user_account (username, password, email, is_admin, created_at)
VALUES (
    'admin',
    '$2a$12$9XaGeI6KUgLalLeVId7A7uCDVD5vI0NhpCfTKHj2iyQlgQf5fpttS', -- senha: 1234
    'admin@empresa.com',
    true,
    NOW()
);
```

Execução da Aplicação
Após configurar o Tomcat e o PostgreSQL, siga estas etapas para iniciar a aplicação:

Verifique se o banco de dados está em funcionamento.

Faça o deploy da aplicação no Tomcat.

Acesse a aplicação em http://localhost:8080/.

Faça login com o usuário admin (username: admin, senha: 1234).

A partir daí, você poderá visualizar e gerenciar os dados dos funcionários, calcular o IRRF e gerar relatórios de folha de pagamento.

