# Quarto Trabalho – Redes I

Este repositório contém a implementação do **Quarto Trabalho da matéria Redes I**.

**Nota:** 75/100  
**Observações:** Não foi uma boa ideia fazer o receptor ficar esperando o tempo de um quadro para decidir se para ou não de esperar, pode acabar facilmente entrando num loop

**Professor:** Marlos Marques  
**Semestre:** 2025.2

---

## Descrição

Este trabalho é uma continuação do Terceiro Trabalho de Redes I.

Permite a comunicação em ambos sentidos:  

* Sentido 1: computador → impressora (envia mensagem para ser impressa)

* Sentido 2: impressora → computador (scanner envia texto para ser digitalizado)  

E foram implementados e explorados os seguintes tipos de controle de fluxo:

* Janela deslizante de 1 bit

* Janela deslizante Go-Back-N

* Janela deslizante com retransmissão seletiva

---

## Para executar o trabalho

Tenha o **Java 8** instalado.

### Verificar se o Java está instalado corretamente:

```bash
java -version
javac -version
```

Deve aparecer algo semelhante a:

```text
java version "1.8.0_431"
```

---

## Como compilar e executar:

1. Acesse, pelo terminal, a pasta onde está o arquivo principal do projeto.

   (A pasta contém um arquivo `Principal.java`.)

2. Compile o código:

```bash
javac Principal.java
```

3. Execute o programa:

```bash
java Principal
```
  
