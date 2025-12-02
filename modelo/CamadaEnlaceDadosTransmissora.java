/* ***************************************************************
* Autor............: Isis Caroline Lima Viana
* Matricula........: 202410016
* Inicio...........: 16/09/2025
* Ultima alteracao.: 22/11/2025
* Nome.............: CamadaEnlaceDadosTransmissora.java
* Funcao...........: Essa camada eh responsavel por preparar os dados 
* vindos da camada de aplicacao para transmissao. Ela aplica o 
* enquadramento, o controle de erro e o controle de fluxo (com 
* janela deslizante) antes de enviar os quadros para a camada fisica.
*************************************************************** */
package modelo;

//importando as bibliotecas necessarias
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class CamadaEnlaceDadosTransmissora {

  private CamadaFisicaTransmissora camada_Fisica_Transmissora;
  private int tipoDeCodificacao;
  private int tipoDeEnquadramento;
  private int tipoDeControleDeErro;
  private int tipoDeControleDeFluxo;
  private final int contraBarra = 0b01011100, flag = 0b01111110, esc = 0b00011011, g = 0x04C11DB7;
  private int num = 3;
  private Ponteiro[] ponteiros;
  private Temporizador[] temporizadores = null;
  private Semaphore mutex = new Semaphore(1);
  private int contadorTemporizadores = 1, contT = 3;
  private int[] bitsHamming;
  private JanelaDeslizante janelaDeslizante;

  // Construtor:
  public CamadaEnlaceDadosTransmissora(CamadaFisicaTransmissora camada_Fisica_Transmissora, int tipoDeCodificacao,
      int tipoDeEnquadramento, int tipoDeControleDeErro, int tipoDeControleDeFluxo) {
    // camada fisica como parametro
    this.camada_Fisica_Transmissora = camada_Fisica_Transmissora;
    this.tipoDeCodificacao = tipoDeCodificacao;
    this.tipoDeEnquadramento = tipoDeEnquadramento;
    this.tipoDeControleDeErro = tipoDeControleDeErro;
    this.tipoDeControleDeFluxo = tipoDeControleDeFluxo;
    bitsHamming = new int[] { 0, 1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5,
        5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7,
        7, 7, 7 };
  }

  /*
   * ***************************************************************
   * Metodo: CamadaEnlaceDadosTransmissora
   * Funcao: chama o metodo de enquadramento e instancia seu retorno
   * no array quadroEnquadrado. Por fim envia o quadroEnquadrado para
   * a camada fisica transmissora
   * Parametros: int[] quadro (quadro sem enquadramento)
   * Retorno: int[] quadroEnquadrado
   ****************************************************************/
  public void camadaEnlaceDadosTransmissora(int quadro[]) {
    int[] quadroEnquadrado = camadaEnlaceDadosTransmissoraEnquadramento(quadro);
    if (tipoDeEnquadramento != 3) {
      quadroEnquadrado = camadaEnlaceDadosTransmissoraControleDeErro(quadroEnquadrado);
      camadaEnlaceDadosTransmissoraControleDeFluxo(quadroEnquadrado);
    } else {
      camada_Fisica_Transmissora.camadaFisicaTransmissora(quadro);
    }

  }// fim do metodo CamadaEnlaceDadosTransmissora

  ////////////////////////////////////////////////////////////////
  /////////////////////// METODOS DE /////////////////////////////
  ///////////////////// ENQUADRAMENTO ////////////////////////////
  ////////////////////////////////////////////////////////////////

  /*
   * ***************************************************************
   * Metodo: camadaEnlaceDadosTransmissoraEnquadramento
   * Funcao: chama o metodo de enquadramento equivalente ao tipo de
   * enquadramento selecionado no menu.
   * Parametros: int[] quadro (quadro sem enquadramento)
   * Retorno: int[] quadroEnquadrado
   ****************************************************************/
  public int[] camadaEnlaceDadosTransmissoraEnquadramento(int quadro[]) {
    int quadroEnquadrado[];
    switch (tipoDeEnquadramento) {
      case 0: // contagem de caracteres
        quadroEnquadrado = camadaEnlaceDadosTransmissoraEnquadramentoContagemDeCaracteres(quadro);
        break;
      case 1: // insercao de bytes
        quadroEnquadrado = camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBytes(quadro);
        break;
      case 2: // insercao de bits
        quadroEnquadrado = camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBits(quadro);
        break;
      case 3: // violacao da camada fisica
        quadroEnquadrado = camadaEnlaceDadosTransmissoraEnquadramentoViolacaoDaCamadaFisica(quadro);
        break;
      default:
        quadroEnquadrado = quadro;
    }// fim do switch/case
    System.out.println("\nCamada de Enlace de Dados\nEnquadramento:");
    imprimir(quadroEnquadrado);
    // System.out.println("POnteiros:");
    // imprimirPonteiros();
    return quadroEnquadrado;
  }// fim do metodo CamadaEnlaceTransmissoraEnquadramento

  /*
   * ***************************************************************
   * Metodo: CamadaEnlaceDadosTransmissoraContagemDeCaracteres
   * Funcao: Identifica qual numero foi informado em num (mudanca
   * possivel apenas no codigo para fins de escalabilidade). Coloca
   * esse numero no array quadroEnquadrado e inicia um loop onde a
   * cada n bytes insere esse numero n de novo ate que todos os bytes
   * do quadro original estejam no quadroEnquadrado
   * Parametros: int[] quadro (quadro sem enquadramento)
   * Retorno: int[] quadroEnquadrado
   ****************************************************************/
  public int[] camadaEnlaceDadosTransmissoraEnquadramentoContagemDeCaracteres(int quadro[]) {
    // implementacao do algoritmo
    int num = 0b00000011; // 3
    System.out.println("\nContagem de caracteres: " + num);
    int quantosBytes = (quadro.length - 1) * 4 + (4 - bytesVazios(quadro[quadro.length - 1]));
    int quantosCabecalhos = (quantosBytes + (num - 2)) / (num - 1);
    int totalBytes = quantosCabecalhos * 2 + quantosBytes;
    int tamanhoNovoQuadro = (totalBytes + 3) / 4;
    int[] quadroEnquadrado = new int[tamanhoNovoQuadro];
    int contQuadroEnquadrado = 0, contQuadro = 0, k = 32;
    int contInt = 0, contBytesEnquadrados = 0, contadorDeQuadros = 0;
    quadroEnquadrado[0] = 0;
    ponteiros = new Ponteiro[quantosCabecalhos];

    while (quantosCabecalhos != 0) {
      ponteiros[contadorDeQuadros] = new Ponteiro(k - 1, contQuadroEnquadrado);
      k -= 8;
      quadroEnquadrado[contQuadroEnquadrado] |= ((contadorDeQuadros % 10) + 1) << k;
      contBytesEnquadrados++;
      k -= 8;
      if (k < 0)
        k = 24;
      if ((totalBytes - contBytesEnquadrados) < num)
        num = (totalBytes - contBytesEnquadrados);
      quadroEnquadrado[contQuadroEnquadrado] |= num << k;
      contBytesEnquadrados++;
      quantosCabecalhos--;
      for (int i = num; i > 1; i--) { // coloca caracter por caracter no novo quadro
        if (contBytesEnquadrados % 4 == 0) {
          contQuadroEnquadrado++;
          if (contQuadroEnquadrado < quadroEnquadrado.length)
            quadroEnquadrado[contQuadroEnquadrado] = 0;
          else
            break;
          k = 32;
        }
        for (int j = 31; j >= 24; j--) {
          k--;
          int masc = 1 << (j - (contInt * 8));
          if ((masc & quadro[contQuadro]) != 0) {
            quadroEnquadrado[contQuadroEnquadrado] |= 1 << k;
          }
        }

        contBytesEnquadrados++;
        contInt = (contInt + 1) % 4;
        if (contInt == 0) {
          contQuadro++;
        }
        if (contBytesEnquadrados == totalBytes)
          break;

      } // fim do for de contagem de caracteres
      ponteiros[contadorDeQuadros].setFimQuadro(k, contQuadroEnquadrado);
      contadorDeQuadros++;
      if (contBytesEnquadrados % 4 == 0) {
        k = 32;
        contQuadroEnquadrado++;
        if (contQuadroEnquadrado < quadroEnquadrado.length)
          quadroEnquadrado[contQuadroEnquadrado] = 0;
      }

    } // fim do while
    // imprimir(quadroEnquadrado);
    return quadroEnquadrado;
  }// fim do metodo CamadaEnlaceDadosTransmissoraContagemDeCaracteres

  /*
   * ***************************************************************
   * Metodo: camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBytes
   * Funcao: vai copiando os bytes do quadro original para o quadroEnquadrado
   * e entre os quadros insere o caracter de contrabarra(\). Caso a mensagem
   * original possua uma contrabarra insere-se uma outra contrabarra antes
   * daquela na mensagem.
   * Parametros: int[] quadro (quadro sem enquadramento)
   * Retorno: int[] quadroEnquadrado
   ****************************************************************/
  public int[] camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBytes(int quadro[]) {
    // implementacao do algoritmo
    System.out.println("\nInsercao de Bytes: " + num);
    int quantosBytes = (quadro.length - 1) * 4 + (4 - bytesVazios(quadro[quadro.length - 1]));
    int quantosCabecalhos = ((quantosBytes + (num - 2)) / (num - 1)) * 3;
    int totalBytes = quantosCabecalhos + quantosBytes + bucarPadrao(contraBarra, quadro);
    int tamanhoNovoQuadro = (totalBytes + 3) / 4;
    int[] quadroEnquadrado = new int[tamanhoNovoQuadro];
    int[] contQuadroEnquadrado = { 0 };
    int contQuadro = 0, k = 32;
    int contInt = 0, contBytesEnquadrados = 0, contadorDeQuadros = 0;
    quadroEnquadrado[0] = 0;
    System.out.println("Quantos ponteiros:" + (quantosCabecalhos / 2));
    ponteiros = new Ponteiro[(quantosCabecalhos) / 3];
    while (quantosCabecalhos != 0) {
      ponteiros[contadorDeQuadros] = new Ponteiro(k - 1, contQuadroEnquadrado[0]);
      for (int a = 0; a < 8; a++)
        k = decrementa(quadroEnquadrado, contQuadroEnquadrado, k);
      quadroEnquadrado[contQuadroEnquadrado[0]] |= ((contadorDeQuadros % 10) + 1) << k;
      contBytesEnquadrados++;
      quantosCabecalhos--;
      for (int a = 0; a < 8; a++)
        k = decrementa(quadroEnquadrado, contQuadroEnquadrado, k);
      quadroEnquadrado[contQuadroEnquadrado[0]] |= contraBarra << k;
      contBytesEnquadrados++;
      quantosCabecalhos--;
      for (int i = num; i > 1; i--) { // coloca caracter por caracter no novo quadro
        if (contBytesEnquadrados % 4 == 0) {
          contQuadroEnquadrado[0]++;
          if (contQuadroEnquadrado[0] < quadroEnquadrado.length)
            quadroEnquadrado[contQuadroEnquadrado[0]] = 0;
          else
            break;
          k = 32;
        }
        int byteReconhecido = 0;
        for (int j = 31; j >= 24; j--) {
          // k--;
          int masc = 1 << (j - (contInt * 8));
          if ((masc & quadro[contQuadro]) != 0) {

            byteReconhecido |= 1 << (j - 24);
          }
        }
        if (byteReconhecido == contraBarra || byteReconhecido == esc) {
          if (contBytesEnquadrados % 4 == 0) {
            k = 32;
            contQuadroEnquadrado[0]++;
            quadroEnquadrado[contQuadroEnquadrado[0]] = 0;
          }
          k -= 8;
          quadroEnquadrado[contQuadroEnquadrado[0]] |= esc << k;
          contBytesEnquadrados++;
        }

        k -= 8;
        if (k < 0) {
          k = 24;
          contQuadroEnquadrado[0]++;
          if (contQuadroEnquadrado[0] < quadroEnquadrado.length)
            quadroEnquadrado[contQuadroEnquadrado[0]] = 0;
        }
        quadroEnquadrado[contQuadroEnquadrado[0]] |= byteReconhecido << k;

        contBytesEnquadrados++;

        contInt = (contInt + 1) % 4;
        if (contInt == 0) {
          contQuadro++;
        }

        if (contBytesEnquadrados == totalBytes - 1)
          break;

      } // fim do for de contagem de caracteres
      for (int a = 0; a < 8; a++)
        k = decrementa(quadroEnquadrado, contQuadroEnquadrado, k);
      quadroEnquadrado[contQuadroEnquadrado[0]] |= contraBarra << k;
      contBytesEnquadrados++;
      quantosCabecalhos--;
      ponteiros[contadorDeQuadros].setFimQuadro(k, contQuadroEnquadrado[0]);
      ponteiros[contadorDeQuadros].imp();
      contadorDeQuadros++;
    } // fim do while
    // k -= 8;
    // quadroEnquadrado[contQuadroEnquadrado] |= contraBarra << k;
    return quadroEnquadrado;
  }// fim do metodo CamadaEnlaceDadosTransmissoraInsercaoDeBytes

  // viu 5 1's coloca um 0

  /*
   * ***************************************************************
   * Metodo: camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBytes
   * Funcao: No inicio chama a funcao insereBits0 que vai inserir os 0
   * extras. E, depois disso, vai copiando os bites do quadro com zeros
   * extras para o quadroEnquadrado e, a cada byte insere a flag 01111110.
   * Parametros: int[] quadro (quadro sem enquadramento)
   * Retorno: int[] quadroEnquadrado
   ****************************************************************/
  public int[] camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBits(int quadro[]) {
    // implementacao do algoritmo
    System.out.println("\nInsercao de Bytes: " + num);
    int quantosBytes = (quadro.length - 1) * 4 + (4 - bytesVazios(quadro[quadro.length - 1]));

    int zerosInseridos = buscarUns(quadro);
    int[] totalBits = new int[] { 0 };

    quadro = insereBits0(quadro, ((quantosBytes * 8) + zerosInseridos + 31) / 32, totalBits);
    // System.out.println("Com os 0 inseridos:");
    // imprimir(quadro);
    quantosBytes = (quadro.length - 1) * 4 + (4 - bytesVazios(quadro[quadro.length - 1]));
    int quantosCabecalhos = ((quantosBytes + (num - 2)) / (num - 1)) * 3;
    int totalBytes = quantosCabecalhos + quantosBytes;
    int tamanhoNovoQuadro = (totalBytes + 3) / 4;
    int[] quadroEnquadrado = new int[tamanhoNovoQuadro];

    int[] contQuadroEnquadrado = { 0 };
    int contQuadro = 0, k = 32;
    int contInt = 0, contBytesEnquadrados = 0, contadorDeQuadros = 0;
    quadroEnquadrado[0] = 0;

    ponteiros = new Ponteiro[(quantosCabecalhos / 3)];
    while (quantosCabecalhos != 0) {
      System.out.println("Faltam " + quantosCabecalhos + " cabecalhos");
      ponteiros[contadorDeQuadros] = new Ponteiro(k - 1, contQuadroEnquadrado[0]);
      for (int j = 7; j >= 0; j--) {
        k = decrementa(quadroEnquadrado, contQuadroEnquadrado, k);
        int masc = 0 | 1 << j;
        if ((masc & ((contadorDeQuadros % 10) + 1)) != 0)
          quadroEnquadrado[contQuadroEnquadrado[0]] |= 1 << k;
      }
      contBytesEnquadrados++;
      quantosCabecalhos--;
      for (int j = 7; j >= 0; j--) {
        k = decrementa(quadroEnquadrado, contQuadroEnquadrado, k);
        int masc = 0 | 1 << j;
        if ((masc & flag) != 0)
          quadroEnquadrado[contQuadroEnquadrado[0]] |= 1 << k;
      }
      contBytesEnquadrados++;
      quantosCabecalhos--;
      for (int i = num; i > 1; i--) { // coloca caracter por caracter no novo quadro
        if (contBytesEnquadrados % 4 == 0) {
          contQuadroEnquadrado[0]++;
          if (contQuadroEnquadrado[0] < quadroEnquadrado.length)
            quadroEnquadrado[contQuadroEnquadrado[0]] = 0;
          else
            break;
          k = 32;
        }
        int byteReconhecido = 0;
        for (int j = 31; j >= 24; j--) {
          k--;
          int masc = 1 << (j - (contInt * 8));
          if ((masc & quadro[contQuadro]) != 0) {
            quadroEnquadrado[contQuadroEnquadrado[0]] |= 1 << k;
            byteReconhecido |= 1 << (j - 24);
          }
        }
        contBytesEnquadrados++;
        if (byteReconhecido == flag) {
          if (contBytesEnquadrados % 4 == 0) {
            k = 32;
            contQuadroEnquadrado[0]++;
            quadroEnquadrado[contQuadroEnquadrado[0]] = 0;
          }
          for (int j = 7; j >= 0; j--) {
            k = decrementa(quadroEnquadrado, contQuadroEnquadrado, k);
            int masc = 0 | 1 << j;
            if ((masc & flag) != 0)
              quadroEnquadrado[contQuadroEnquadrado[0]] |= 1 << k;
          }
          contBytesEnquadrados++;
        }

        contInt = (contInt + 1) % 4;
        if (contInt == 0) {
          contQuadro++;
        }
        if (contBytesEnquadrados == totalBytes - 1)
          break;

      } // fim do for de contagem de caracteres

      if (contBytesEnquadrados % 4 == 0) {
        k = 32;
        contQuadroEnquadrado[0]++;
        if (contQuadroEnquadrado[0] < quadroEnquadrado.length)
          quadroEnquadrado[contQuadroEnquadrado[0]] = 0;
      }
      k -= 8;
      quadroEnquadrado[contQuadroEnquadrado[0]] |= flag << k;
      contBytesEnquadrados++;
      quantosCabecalhos--;
      ponteiros[contadorDeQuadros].setFimQuadro(k, contQuadroEnquadrado[0]);
      contadorDeQuadros++;
    } // fim do while

    return quadroEnquadrado;
  }// fim do metodo CamadaEnlaceDadosTransmissoraInsercaoDeBits

  /*
   * ***************************************************************
   * Metodo: camadaEnlaceDadosTransmissoraEnquadramentoViolacaoDaCamadaFisica
   * Funcao: Esse metodo de enquadramento delega o enquadramento para a camada
   * fisica, portanto retorna a mesma coisa
   * Parametros: int[] quadro (quadro sem enquadramento)
   * Retorno: int[] quadro (ainda sem enquadramento)
   ****************************************************************/
  public int[] camadaEnlaceDadosTransmissoraEnquadramentoViolacaoDaCamadaFisica(int quadro[]) {
    // implementacao do algoritmo
    System.out.println("a\nViolacao da camada fisica");
    return quadro;
  }// fim do metodo CamadaEnlaceDadosTransmissoraViolacaoDaCamadaFisica

  ////////////////////////////////////////////////////////////////
  /////////////////////// METODOS DE /////////////////////////////
  /////////////////// CONTROLE DE ERRO ///////////////////////////
  ////////////////////////////////////////////////////////////////

  /*
   * ***************************************************************
   * Metodo: camadaEnlaceDadosTransmissoraControleDeErro
   * Funcao: Direciona o quadro enquadrado para o metodo de controle
   * de erro apropriado, com base na variavel `tipoDeControleDeErro`.
   * Parametros: int[] quadro (quadro ja enquadrado)
   * Retorno: int[] (quadro com os bits de controle de erro adicionados)
   ****************************************************************/
  public int[] camadaEnlaceDadosTransmissoraControleDeErro(int quadro[]) {
    switch (tipoDeControleDeErro) {
      case 0: // bit de paridade par
        return camadaEnlaceDadosTransmissoraControleDeErroBitParidadePar(quadro);
      case 1: // bit de paridade impar
        return camadaEnlaceDadosTransmissoraControleDeErroBitParidadeImpar(quadro);
      case 2: // CRC
        return camadaEnlaceDadosTransmissoraControleDeErroCRC(quadro);
      case 3: // codigo de Hamming
        return camadaEnlaceDadosTransmissoraControleDeErroCodigoDeHamming(quadro);
      default:
        return quadro;
    }// fim do switch/case

  }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeErro

  /*
   * ***************************************************************
   * Metodo: camadaEnlaceDadosTransmissoraControleDeErroBitParidadePar
   * Funcao: Adiciona um bit de paridade par a cada quadro. Conta os
   * bits '1' e adiciona '1' se a contagem for impar, ou '0' se for par.
   * Parametros: int[] quadro (quadro enquadrado)
   * Retorno: int[] (novo quadro com bits de paridade par)
   ****************************************************************/
  public int[] camadaEnlaceDadosTransmissoraControleDeErroBitParidadePar(int quadro[]) {
    // implementacao do algoritmo
    int quantosBits = 0;
    for (Ponteiro p : ponteiros) {
      quantosBits += p.tamanhoQuadro() + 1;
    }
    int tamanho = (quantosBits + 31) / 32;
    int[] novoQuadro = new int[tamanho];

    int[] contNovoQuadro = { 0 };
    int k = 32, cont = 0;
    novoQuadro[0] = 0;
    for (int i = 0; i < ponteiros.length; i++) {
      int j = ponteiros[i].intInicio() + 1;
      int[] contQuadro = { ponteiros[i].arrayInicio() };
      cont = 0;
      k = decrementa(novoQuadro, contNovoQuadro, k);
      ponteiros[i].setInicioQuadro(k, contNovoQuadro[0]);
      while (j != ponteiros[i].intFim() || contQuadro[0] != ponteiros[i].arrayFim()) {
        j = decrementa(contQuadro, j);
        int masc = 0 | 1 << j;
        if ((masc & quadro[contQuadro[0]]) != 0) {
          novoQuadro[contNovoQuadro[0]] |= 1 << k;
          cont++;
        }
        k = decrementa(novoQuadro, contNovoQuadro, k);
      }
      if (cont % 2 != 0) { // impar
        novoQuadro[contNovoQuadro[0]] |= 1 << k;
      }
      ponteiros[i].setFimQuadro(k, contNovoQuadro[0]);
    }
    System.out.println("\nControle de erros:");
    imprimir(novoQuadro);
    return novoQuadro;
  }// fim do metodo CamadaEnlaceDadosTransmissoraControledeErroBitParidadePar

  /*
   * ***************************************************************
   * Metodo: camadaEnlaceDadosTransmissoraControleDeErroBitParidadeImpar
   * Funcao: Adiciona um bit de paridade impar a cada quadro. Conta os
   * bits '1' e adiciona '1' se a contagem for par, ou '0' se for impar.
   * Parametros: int[] quadro (quadro enquadrado)
   * Retorno: int[] (novo quadro com bits de paridade impar)
   ****************************************************************/
  public int[] camadaEnlaceDadosTransmissoraControleDeErroBitParidadeImpar(int quadro[]) {
    // implementacao do algoritmo
    int quantosBits = 0;
    for (Ponteiro p : ponteiros) {
      quantosBits += p.tamanhoQuadro() + 1;
    }
    int tamanho = (quantosBits + 31) / 32;
    int[] novoQuadro = new int[tamanho];

    int[] contNovoQuadro = { 0 };
    int k = 32, cont = 0;
    novoQuadro[0] = 0;
    for (int i = 0; i < ponteiros.length; i++) {
      int j = ponteiros[i].intInicio() + 1;
      int[] contQuadro = { ponteiros[i].arrayInicio() };
      cont = 0;
      k = decrementa(novoQuadro, contNovoQuadro, k);
      ponteiros[i].setInicioQuadro(k, contNovoQuadro[0]);
      while (j != ponteiros[i].intFim() || contQuadro[0] != ponteiros[i].arrayFim()) {
        j = decrementa(contQuadro, j);
        int masc = 0 | 1 << j;
        if ((masc & quadro[contQuadro[0]]) != 0) {
          novoQuadro[contNovoQuadro[0]] |= 1 << k;
          cont++;
        }
        k = decrementa(novoQuadro, contNovoQuadro, k);
      }
      if (cont % 2 == 0) { // par
        novoQuadro[contNovoQuadro[0]] |= 1 << k;
      }
      ponteiros[i].setFimQuadro(k, contNovoQuadro[0]);
    }
    System.out.println("\nControle de erros:");
    imprimir(novoQuadro);
    return novoQuadro;
  }// fim do metodo CamadaEnlaceDadosTransmissoraControledeErroBitParidadeImpar

  /*
   * ***************************************************************
   * Metodo: camadaEnlaceDadosTransmissoraControleDeErroCRC
   * Funcao: Calcula e anexa o CRC de 32
   * bits a cada quadro. Para cada quadro, calcula o resto
   * (usando encontraResto) e o anexa ao final.
   * Parametros: int[] quadro (quadro enquadrado)
   * Retorno: int[] (novo quadro com CRC anexado)
   ****************************************************************/
  public int[] camadaEnlaceDadosTransmissoraControleDeErroCRC(int quadro[]) {
    // implementacao do algoritmo
    int quantosBits = 0;
    for (Ponteiro p : ponteiros) {
      quantosBits += p.tamanhoQuadro() + 32;
    }
    int tamanho = (quantosBits + 31) / 32;
    int[] novoQuadro = new int[tamanho];
    // usar polinomio CRC-32(IEEE 802)
    int[] contNovoQuadro = { 0 };
    int k = 32;
    Arrays.fill(novoQuadro, 0);
    for (int i = 0; i < ponteiros.length; i++) {
      int[] contQuadro = { ponteiros[i].arrayInicio() };
      int j = ponteiros[i].intInicio() + 1; // Ponteiro para o quadro original

      int tamanhoCopia = (ponteiros[i].tamanhoQuadro() + 31) / 32;
      int[] copiaQuadro = new int[tamanhoCopia], contCopia = { 0 };
      int l = 32; // Ponteiro para a copiaQuadro
      Arrays.fill(copiaQuadro, 0);

      // atualiza o ponteiro para ele guardar o novo inicio do quadro
      k = decrementa(novoQuadro, contNovoQuadro, k);
      ponteiros[i].setInicioQuadro(k, contNovoQuadro[0]);

      while (true) {
        j = decrementa(contQuadro, j);
        l = decrementa(contCopia, l);
        // Copia o bit
        int masc = 0 | 1 << j;
        if ((masc & quadro[contQuadro[0]]) != 0) {
          novoQuadro[contNovoQuadro[0]] |= 1 << k;
          copiaQuadro[contCopia[0]] |= 1 << l;
        }
        // Checa se este era o ultimo bit
        if (j == ponteiros[i].intFim() && contQuadro[0] == ponteiros[i].arrayFim()) {
          break; // sai do loop
        }
        // Se nao foi o ultimo, vai para a proxima posicao
        k = decrementa(novoQuadro, contNovoQuadro, k);
      }
      // k na primeira posicao do CRC
      k = decrementa(novoQuadro, contNovoQuadro, k);

      // adicionar o resto no fim do quadro
      int resto = encontraResto(copiaQuadro);
      for (int ii = 31; ii >= 0; ii--) {
        int masc = 0 | 1 << ii;
        if ((resto & masc) != 0) {
          novoQuadro[contNovoQuadro[0]] |= 1 << k;
        }
        if (ii != 0)
          k = decrementa(novoQuadro, contNovoQuadro, k);
      } // fim do for

      // atualiza o ponteiro para ele guardar o novo fim do quadro
      ponteiros[i].setFimQuadro(k, contNovoQuadro[0]);

    } // fim do for dos ponteiros

    System.out.println("\nControle de erros:");
    imprimir(novoQuadro);

    return novoQuadro;
  } // fim da camadaEnlaceDadosTransmissoraControleDeErroCRC

  /*
   * ***************************************************************
   * Metodo: camadaEnlaceDadosTransmissoraControleDeErroCodigoDeHamming
   * Funcao: Aplica o codigo de Hamming. Primeiro, chama
   * quadroComPotenciasVazias para abrir espaco para os bits de
   * paridade. Em seguida, calcula e insere os bits de paridade
   * corretos nas posicoes de potencia de 2.
   * Parametros: int[] quadro (quadro enquadrado)
   * Retorno: int[] (novo quadro com bits de Hamming inseridos)
   ****************************************************************/
  public int[] camadaEnlaceDadosTransmissoraControleDeErroCodigoDeHamming(int quadro[]) {
    quadro = quadroComPotenciasVazias(quadro);
    int[] contQuadro = { 0 };
    int j = 32;

    for (int i = 0; i < ponteiros.length; i++) {

      int n = 0;
      int[] contQuadroAux = new int[] { contQuadro[0] };
      int aux = decrementa(contQuadroAux, j);
      ponteiros[i].setInicioQuadro(aux, contQuadro[0]);

      while (j != ponteiros[i].intFim() || contQuadro[0] != ponteiros[i].arrayFim()) {
        n++;
        j = decrementa(contQuadro, j);
        boolean ehPotenciaDe2 = (n & (n - 1)) == 0; // ex: 100 & 011 = 0
        if (ehPotenciaDe2) {
          int a = (contaUns(quadro, ponteiros[i], n));
          if (a % 2 != 0)
            quadro[contQuadro[0]] |= 1 << j;
        }
      } // fim do while

      ponteiros[i].setFimQuadro(j, contQuadro[0]);

    } // fim do for dos ponteiros

    System.out.println("\nControle de erros:");
    imprimir(quadro);
    return quadro;
  }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeErroCodigoDehamming

  ////////////////////////////////////////////////////////////////
  /////////////////////// METODOS DE /////////////////////////////
  /////////////////// CONTROLE DE FLUXO //////////////////////////
  ////////////////////////////////////////////////////////////////

  public void camadaEnlaceDadosTransmissoraControleDeFluxo(int quadro[]) {
    // int tipoDeControleDeFluxo = 1; //alterar de acordo com o teste
    switch (tipoDeControleDeFluxo) {
      case 0: // protocolo de janela deslizante de 1 bit
        camadaEnlaceDadosTransmissoraJanelaDeslizanteUmBit(quadro);
        break;
      case 1: // protocolo de janela deslizante go-back-n
        camadaEnlaceDadosTransmissoraJanelaDeslizanteGoBackN(quadro);
        break;
      case 2: // protocolo de janela deslizante com retransmissao seletiva
        camadaEnlaceDadosTransmissoraJanelaDeslizanteComRetransmissaoSeletiva(quadro);
        break;
    }// fim do switch/case
  }

  /*
   * ***************************************************************
   * Metodo: camadaEnlaceDadosTransmissoraJanelaDeslizanteUmBit
   * Funcao: Gerencia a transmissao dos quadros enviando e esperando
   * (Stop-and-Wait). Para cada quadro, cria um Temporizador.
   * Envia o primeiro quadro e espera por um ACK. Se o ACK chegar
   * envia o proximo. Se o temporizador estourar, envia de novo.
   * Parametros: int[] quadro (quadro final com enquadramento e erro)
   * Retorno: void
   ****************************************************************/
  public void camadaEnlaceDadosTransmissoraJanelaDeslizanteUmBit(int quadro[]) {

    criarTemporizadores(quadro);

    // ativa o primeiro temporizador
    if (temporizadores.length > 0) {
      temporizadores[0].enviar();
      temporizadores[0].start();
    }

  }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeFluxo

  public void camadaEnlaceDadosTransmissoraJanelaDeslizanteGoBackN(int[] quadro) {
    criarTemporizadores(quadro);
    // ativa o primeiro temporizador
    if (temporizadores.length > 0) {
      if (temporizadores.length == 1)
        janelaDeslizante = new JanelaDeslizante(temporizadores[0], null, null);
      else if (temporizadores.length == 2)
        janelaDeslizante = new JanelaDeslizante(temporizadores[0], temporizadores[1], null);
      else
        janelaDeslizante = new JanelaDeslizante(temporizadores[0], temporizadores[1], temporizadores[2]);
      janelaDeslizante.enviar();
    }
  }

  public void camadaEnlaceDadosTransmissoraJanelaDeslizanteComRetransmissaoSeletiva(int[] quadro) {
    criarTemporizadores(quadro);
    // ativa o primeiro temporizador
    if (temporizadores.length > 0) {
      if (temporizadores.length == 1)
        janelaDeslizante = new JanelaDeslizante(temporizadores[0], null, null);
      else if (temporizadores.length == 2)
        janelaDeslizante = new JanelaDeslizante(temporizadores[0], temporizadores[1], null);
      else
        janelaDeslizante = new JanelaDeslizante(temporizadores[0], temporizadores[1], temporizadores[2]);
      janelaDeslizante.enviar();
    }
  }

  ////////////////////////////////////////////////////////////////
  //////////////////////// METODOS ///////////////////////////////
  ////////////////////// AUXILIARES //////////////////////////////
  ////////////////////////////////////////////////////////////////

  public void criarTemporizadores(int[] quadro) {
    contadorTemporizadores = 1;
    temporizadores = new Temporizador[ponteiros.length];
    Arrays.fill(temporizadores, null);

    // separa os quadros e relaciona cada um com um temporizador
    System.out.println("Tamanho total:" + quadro.length);
    for (int i = 0; i < ponteiros.length; i++) {
      int[] contQuadro = { ponteiros[i].arrayInicio() };
      int j = ponteiros[i].intInicio() + 1;

      int tamanhoQuadro = 0;
      tamanhoQuadro = (ponteiros[i].tamanhoQuadro() + 8 + 31) / 32;

      int[] copiaQuadro = new int[tamanhoQuadro], contCopia = { 0 };
      int l = 32;
      // copiaQuadro[0] = ((i%10)+1)<<24;
      copiaQuadro[0] = 0;
      // copia o quadro para o copiaQuadro
      while (true) {
        j = decrementa(contQuadro, j);
        l = decrementa(copiaQuadro, contCopia, l);

        int masc = 0 | 1 << j;
        if ((masc & quadro[contQuadro[0]]) != 0) {
          copiaQuadro[contCopia[0]] |= 1 << l;
        }
        if (j == ponteiros[i].intFim() && contQuadro[0] == ponteiros[i].arrayFim()) {
          break;
        }
      } // fim do while

      // cria novo temporizador para esse quadro:
      temporizadores[i] = new Temporizador(copiaQuadro);
    } // fim do for dos ponteiros
  }

  /*
   * ***************************************************************
   * Metodo: quadroComPotenciasVazias
   * Funcao: Prepara os quadros para o Codigo de Hamming, inserindo
   * bits 0 nas posicoes que sao potencias de 2 (1, 2, 4, 8, ...),
   * deslocando os bits de dados originais.
   * Parametros: int[] quadro (quadro de dados)
   * Retorno: int[] (novo quadro com espacos para os bits de Hamming)
   ****************************************************************/
  public int[] quadroComPotenciasVazias(int[] quadro) {
    int quantosBits = 0;
    for (Ponteiro p : ponteiros) {
      quantosBits += p.tamanhoQuadro() + bitsHamming[p.tamanhoQuadro() - 1];
    }
    int tamanho = (quantosBits + 31) / 32;
    int[] novoQuadro = new int[tamanho];

    int[] contNovoQuadro = { 0 };
    int k = 32;
    novoQuadro[0] = 0;

    for (int i = 0; i < ponteiros.length; i++) {
      int n = 0;
      int[] contQuadro = { ponteiros[i].arrayInicio() };
      int j = ponteiros[i].intInicio() + 1;

      int[] contQuadroAux = { contNovoQuadro[0] };
      int aux1 = decrementa(contQuadroAux, k);
      // System.out.println("Atualizou"); ponteiros[i].imp();
      ponteiros[i].setInicioQuadro(aux1, contQuadroAux[0]);

      while (j != ponteiros[i].intFim() || contQuadro[0] != ponteiros[i].arrayFim()) {
        k = decrementa(novoQuadro, contNovoQuadro, k);
        n++;
        boolean ehPotenciaDe2 = (n & (n - 1)) == 0; // ex: 100 & 011 = 0
        if (!ehPotenciaDe2) {
          j = decrementa(contQuadro, j);
          int masc = 0 | 1 << j;
          if ((masc & quadro[contQuadro[0]]) != 0)
            novoQuadro[contNovoQuadro[0]] |= 1 << k;
        }
      } // fim do while

      // atualiza fim do ponteiro:
      ponteiros[i].setFimQuadro(k, contNovoQuadro[0]);
      // System.out.println("Para: "); ponteiros[i].imp();
    } // fim do for dos ponteiros

    return novoQuadro;
  } // fim do quadroComPotenciasVazias

  /*
   * ***************************************************************
   * Metodo: ACKtemporizador
   * Funcao: Processa um ACK recebido. Se for valido e corresponder
   * ao quadro atual ele para (libera) o temporizador correspondente
   * e inicia o temporizador para o proximo quadro na fila.
   * Parametros: int[] ack (quadro de ACK recebido)
   * Retorno: void
   ****************************************************************/
  public void ACKtemporizador(int[] ack) {

    System.out.println("ACK:");
    imprimir(ack);
    // vou ter que fazer um pra cada tipo de controle de fluxo
    // Verifica se o ACK eh valido E se o timer que ele se refere eh o que ta
    // primeiro na fila

    switch (tipoDeControleDeFluxo) {
      case 0: {
        if (ack[0] != 0 && (contadorTemporizadores - 1) < temporizadores.length
            && !temporizadores[contadorTemporizadores - 1].isLiberado()) {

          temporizadores[contadorTemporizadores - 1].liberar();
          contadorTemporizadores++;

          System.out.println("Fim do temporizador " + (contadorTemporizadores - 1));

          // Se houver um proximo timer, inicie ele
          if (contadorTemporizadores - 1 < temporizadores.length) {
            temporizadores[contadorTemporizadores - 1].enviar();
            temporizadores[contadorTemporizadores - 1].start(); // chama o proximo temporizador
          }
        }
        break;
      }
      case 1: {
        int numSequencia = (ack[0] & 0x7F000000) >>> 24; // remove primeiro byte
        if ((ack[0] & 0x00800000) != 0) { // eh um ack
          if (numSequencia == contadorTemporizadores) { // eh o esperado
            contadorTemporizadores = (contadorTemporizadores) % 10 + 1; // avanca 1
            if (contT >= temporizadores.length) {
              janelaDeslizante.deslizaJanela(null);
            } else {
              janelaDeslizante.deslizaJanela(temporizadores[contT]);
              temporizadores[contT] = null;
              contT++;
            }
          }
        }
        break;
      }
      case 2: {
        int numSequencia = (ack[0] & 0x7F000000) >>> 24;
        boolean ehAck = (ack[0] & 0x00800000) != 0; // Verifica bit de ACK

        if (ehAck) {
          System.out.println("Recebi ACK do quadro " + numSequencia);

          // Verifica se o ACK corresponde a algum timer ativo na janela
          if (janelaDeslizante != null) {
            janelaDeslizante.receberAckSeletivo(numSequencia);
          }
        } else {
          // Se for NACK (bit de ack eh 0)
          if(janelaDeslizante!=null && janelaDeslizante.getTemp1().getQuadro()==numSequencia){
            janelaDeslizante.enviar();
          }
        }
        break;
      }
    }

  } // fim do ACKtemporizador

  /*
   * ***************************************************************
   * Metodo: contaUns
   * Funcao: Conta o numero de bits 1 dentro de um quadro
   * (delimitado pelo Ponteiro). Usado para Hamming: considera
   * apenas os bits cuja posicao n faz (k & n) == k.
   * Parametros: int[] quadro (array de dados), Ponteiro ponteiro
   * (delimitador do quadro), int k (mascara de bits de Hamming).
   * Retorno: int (contagem de bits 1)
   ****************************************************************/
  public int contaUns(int[] quadro, Ponteiro ponteiro, int k) {
    int cont = 0, j = ponteiro.intInicio() + 1, n = 1;
    int[] i = { ponteiro.arrayInicio() };

    while (j != ponteiro.intFim() || i[0] != ponteiro.arrayFim()) {
      j = decrementa(i, j);
      int masc = 0 | 1 << j;
      if ((k & n) == k) {// 100 & 101
        if ((masc & quadro[i[0]]) != 0) {
          cont++;
        }
      }
      n++;
    } // fim do while

    return cont;
  } // fim do contaUns

  /*
   * ***************************************************************
   * Metodo: decrementa (com int[] quadro)
   * Funcao: Decrementa um ponteiro de bit. Se k < 0,
   * reseta para 31 e avanca o ponteiro de array (contQuadro) e
   * zera a nova posicao do array quadro. (Usado para escrita).
   * Parametros: int[] quadro (array sendo escrito), int[] contQuadro
   * (ponteiro de indice), int k (ponteiro de bit).
   * Retorno: int (o novo valor de 'k')
   ****************************************************************/
  // decrementa zerando o quadro a que o ponteiro k faz referencia
  public int decrementa(int[] quadro, int[] contQuadro, int k) {
    k--;
    if (k < 0) {
      k = 31;
      contQuadro[0]++;
      if (contQuadro[0] < quadro.length)
        quadro[contQuadro[0]] = 0;
    }
    return k;
  } // fim do decrementa

  /*
   * ***************************************************************
   * Metodo: decrementa (sem int[] quadro)
   * Funcao: Decrementa um ponteiro de bit . Se k < 0,
   * reseta para 31 e avanca o ponteiro de array (contQuadro).
   * (Usado para leitura, nao modifica o array).
   * Parametros: int[] contQuadro (ponteiro de indice), int k
   * (ponteiro de bit).
   * Retorno: int (o novo valor de k)
   ****************************************************************/
  public int decrementa(int[] contQuadro, int k) {
    k--;
    if (k < 0) {
      k = 31;
      contQuadro[0]++;
    }
    return k;
  } // fim do decrementa

  /*
   * ***************************************************************
   * Metodo: encontraResto
   * Funcao: Calcula o resto do CRC-32 (polinomio g) para o quadro.
   * Simula a divisao polinomial bit a bit e, ao
   * final, adiciona bits 0 ate que o resto atinja 32 bits
   * Parametros: int[] quadro (quadro de dados)
   * Retorno: int (o resto do CRC de 32 bits)
   ****************************************************************/
  public int encontraResto(int[] quadro) {
    int resto = 0; // Registrador CRC

    int totalBits = quadro.length * 32 - (bytesVazios(quadro[quadro.length - 1]) * 8);
    int contQuadro = 0;

    for (int i = 0; i < quadro.length; i++) {
      for (int j = 31; j >= 0 && contQuadro < totalBits; j--, contQuadro++) {

        boolean msb = (resto & 0x80000000) != 0;

        // Pega o proximo bit da mensagem
        int proximoBit = (quadro[i] >>> j) & 1;

        // Desloca o registrador para a esquerda e insere o proximo bit
        resto = (resto << 1) | proximoBit;

        // Faz o XOR com o polinomio
        if (msb) {
          resto ^= g;
        }
      } // fim do for interno
    } // fim do for externo

    // adiciona os bits 0 para completar 32 bits
    for (int k = 0; k < 32; k++) {
      // Pega o MSB
      boolean msb = (resto & 0x80000000) != 0;

      // Desloca para a esquerda
      resto <<= 1;

      // Se o MSB era 1, faz o XOR
      if (msb) {
        resto ^= g;
      }
    }

    System.out.println("RESTO (Transmissor):");
    imprimir(new int[] { resto });
    return resto;
  } // fim do encontraResto

  /*
   * ***************************************************************
   * Metodo: bytesVazios
   * Funcao: Conta quantos bytes vazios tem num int
   * Parametros: int ultimoInt (o inteiro a ser verificado)
   * Retorno: int (numero de bytes vazios, de 0 a 3)
   ****************************************************************/
  public int bytesVazios(int ultimoInt) {
    int i = 0;
    for (i = 0; i < 4; i++) {
      int mascara = 0;
      mascara = 0b11111111 << (i * 8);
      if ((mascara & ultimoInt) != 0)
        break;
    } // fim do for
    return i;
  } // fim do bytesVazios

  /*
   * ***************************************************************
   * Metodo: bucarPadrao
   * Funcao: conta quantas vezes um padrao apareceu no quadro
   * Parametros: int padrao (o byte a ser procurado), int[] quadro
   * Retorno: int (a contagem de ocorrencias)
   ****************************************************************/
  public int bucarPadrao(int padrao, int[] quadro) {
    int cont = 0;
    for (int i = 0; i < quadro.length; i++) {
      for (int j = 0; j < 4; j++) {
        int byteReconhecido = 0;
        for (int k = 31; k >= 24; k--) {
          int masc = 1 << (k - (j * 8));
          if ((masc & quadro[i]) != 0)
            byteReconhecido |= 1 << (k - 24);
        }
        if (byteReconhecido == padrao)
          cont++;
      }
    }
    return cont;
  }

  /*
   * ***************************************************************
   * Metodo: buscarUns
   * Funcao: conta quantos 0 deverao ser inseridos (quantas sequencias
   * 11111 a mensagem tem)
   * Parametros: int[] quadro
   * Retorno: int (a contagem de sequencias "11111")
   ****************************************************************/
  public int buscarUns(int[] quadro) {
    int cont = 0;
    ArrayDeque<Boolean> sequenciaBits = new ArrayDeque<>(5);
    for (int i = 0; i < quadro.length; i++) {
      for (int j = 31; j > 0; j--) {
        if (sequenciaBits.size() == 5)
          sequenciaBits.removeFirst();
        int masc = 1 << j;
        if ((masc & quadro[i]) != 0) {
          sequenciaBits.addLast(true);
        } else {
          sequenciaBits.addLast(false);
        }
        if (sequenciaBits.size() == 5 && sequenciaBits.stream().allMatch(b -> b)) {
          cont++;
          sequenciaBits.clear(); // ou continue monitorando
        }
      }
    }
    return cont;
  }

  /*
   * ***************************************************************
   * Metodo: insereBits0
   * Funcao: Transfere bit a bit do quadro original para o novoQuadro
   * ao mesmo tempo em que vai preenchendo um deque circular de 5
   * posicoes com dados booleanos (true se inseriu 1, false se inseriu 0)
   * se todas as posicoes do deque forem true significa que a mensagem teve
   * 11111 e, por isso insere um 0 no novoQuadro.
   * Parametros: int[] quadro (quadro sem enquadramento)
   * Retorno: int[] quadro com 0s
   ****************************************************************/
  public int[] insereBits0(int[] quadro, int tamanho, int[] cont) {
    int[] novoQuadro = new int[tamanho];
    int contNovoQuadro = 0, k = 32;
    novoQuadro[0] = 0;
    ArrayDeque<Boolean> sequenciaBits = new ArrayDeque<>(5);
    for (int i = 0; i < quadro.length; i++) {
      for (int j = 31; j >= 0; j--) {
        if (sequenciaBits.size() == 5)
          sequenciaBits.removeFirst();
        int masc = 0 | 1 << j;
        k--;
        if ((masc & quadro[i]) != 0) {
          sequenciaBits.addLast(true);
          novoQuadro[contNovoQuadro] |= 1 << k;
        } else {
          sequenciaBits.addLast(false);
        }
        cont[0]++;
        if (sequenciaBits.size() == 5 && sequenciaBits.stream().allMatch(b -> b)) {
          k--; // adiciona 0
          sequenciaBits.clear(); // ou continue monitorando
          cont[0]++;
        }
        if (k == 0) {
          contNovoQuadro++;
          if (contNovoQuadro < novoQuadro.length) {
            novoQuadro[contNovoQuadro] = 0;
            k = 32;
          } // fim do if
        } // fim do if k=0
      } // fim do for j (loop int do quadro)
    } // fim do for i (loop das posicoes do quadro)
    return novoQuadro;
  } // fim do metodo

  public int lerNumSequencia(int[] quadro){
    if(tipoDeControleDeErro!=3){
      return lerNumero(quadro[0]);
    }else{
      int num=0;
      num|=(quadro[0]&0x20000000)>>>22;
      imprimir(new int[]{num});
      num|=(quadro[0]&0x0E000000)>>>21;
      imprimir(new int[]{num});
      num|=(quadro[0]&0x00F00000)>>>20;
      imprimir(new int[]{num});
      System.out.println("Li:"+num);
      return num;
    }
  }

  /*
   * ***************************************************************
   * Metodo: lerNumero
   * Funcao: Le os primeiros 8 bits (o primeiro byte) de um
   * inteiro e o retorna como um numero
   * Parametros: int primeiroInt
   * Retorno: int (o valor do primeiro byte)
   ****************************************************************/
  public int lerNumero(int primeiroInt) {
    int num = 0;
    for (int i = 31; i >= 24; i--) {
      int masc = 0 | 1 << i;
      if ((masc & primeiroInt) != 0)
        num |= 1 << (i - 24);
    }
    return num;
  }

  /*
   * ***************************************************************
   * Metodo: parar
   * Funcao: chama o metodo parar da camada fisica
   * Parametros: void
   * Retorno: void
   ****************************************************************/
  public void parar() {
    if (temporizadores != null) {
      for (Temporizador t : temporizadores) {
        if (t != null)
          t.liberar();
      }
    }
    if (janelaDeslizante != null)
      janelaDeslizante.matarTemps();
    janelaDeslizante = null;
    camada_Fisica_Transmissora.parar();
  }

  /*
   * ***************************************************************
   * Metodo: setTaxaDeErro
   * Funcao: transfere a nova taxa de erro para a camada fisica
   * Parametros: int taxa (o valor da taxa de erro)
   * Retorno: void
   ****************************************************************/
  public void setTaxaDeErro(int taxa) {
    camada_Fisica_Transmissora.setTaxaDeErro(taxa);
  }

  ////////////////////////////////////////////////////////////////
  ////////////////////// METODOS DE //////////////////////////////
  ////////////////////// IMPRESSAO ///////////////////////////////
  ////////////////////////////////////////////////////////////////
  public void imprimir(int[] vetor) {
    for (int a : vetor) {
      String bits32 = String.format("%32s", Integer.toBinaryString(a)).replace(' ', '0');
      System.out.println(bits32);
    }
  }

  public void imprimirPonteiros() {
    for (int i = 0; i < ponteiros.length; i++) {
      System.out.println("Quadro " + i + " -> Inicio: ("
          + ponteiros[i].arrayInicio() + "," + ponteiros[i].intInicio() + ") | Fim: ("
          + ponteiros[i].arrayFim() + "," + ponteiros[i].intFim() + ")");
    }
  }

  ////////////////////////////////////////////////////////////////
  ////////////////////// TEMPORIZADOR ////////////////////////////
  ////////////////////////////////////////////////////////////////
  private class Temporizador extends Thread {
    private int[] quadro;
    private volatile boolean liberado;
    private boolean ehTemp1;

    public Temporizador(int[] quadro) {
      this.quadro = quadro;
      liberado = false;
      this.setDaemon(true);
      ehTemp1 = false;
    }

    //usado para a copia
    public Temporizador(int[] quadro, boolean ehTemp1) {
      this.quadro = quadro;
      liberado = false;
      this.setDaemon(true);
      this.ehTemp1 = ehTemp1;
    }

    public Temporizador copia() {
      return new Temporizador(quadro, ehTemp1);
    }

    /*
     * ***************************************************************
     * Metodo: run
     * Funcao: Adquire um mutex, envia o quadro para a camada fisica.
     * Entra em um loop de espera. Se nao for liberado (nao receber ACK)
     * dentro do tempo, reenvia o quadro e repete a espera.
     * Parametros: void
     * Retorno: void
     ****************************************************************/
    public void run() {
      while (!liberado) {
        synchronized (this) {
          try {
            //espera:
            wait(calcularTempo());
          } catch (InterruptedException e) {
            break;
          }
        }
        //verifica se liberou
        if (liberado)
          break;

        if (!liberado) { // envia de novo
          if (tipoDeControleDeFluxo != 1)
            enviar();
          else if (ehTemp1) {
            janelaDeslizante.enviar();
          }
        } //enviou de novo

      } //fim do while
    } //fim do run

    public void enviar() {
      // mutex camada fisica
      System.out.println("Enviou quadro " + getQuadro());
      try {
        mutex.acquire();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      // envia quadro:
      camada_Fisica_Transmissora.camadaFisicaTransmissora(quadro);

      mutex.release();
    }

    //o tempo que cada temporizador espera varia de acordo com a 
    //confuguracao escolhida para que nao seja curto demais ou 
    //longo demais
    private int calcularTempo() {
      int tamanho = quadro.length * 32;
      int seguranca = 1000;
      int mult = (tipoDeCodificacao != 0) ? 2 : 1;
      if (tipoDeControleDeFluxo != 0) {
        mult = 3;
        seguranca = 3000;
      }
      System.out.println("\nTempo:" + (tamanho * 50 * mult * num + seguranca));
      return tamanho * 50 * mult * num + seguranca;
    }

    /*
     * ***************************************************************
     * Metodo: liberar
     * Funcao: Define a flag liberado como true e interrompe
     * a thread, sinalizando que um ACK foi recebido.
     * Parametros: void
     * Retorno: void
     ****************************************************************/
    public void liberar() {
      synchronized (this) {
        liberado = true;
        this.notify();
      }
    }

    public boolean isLiberado() {
      return liberado;
    }

    public int getQuadro() {
      return lerNumSequencia(quadro);
    }

    public void setComoTemp1(boolean ehTemp1) {
      this.ehTemp1 = ehTemp1;
    }
  } // fim da classe Temporizador


  ////////////////////////////////////////////////////////////////
  ///////////////////// JANELA DESLIZANTE ////////////////////////
  ////////////////////////////////////////////////////////////////
  public class JanelaDeslizante {
    private Temporizador temp1;
    private Temporizador temp2;
    private Temporizador temp3;

    public JanelaDeslizante(Temporizador temp1, Temporizador temp2, Temporizador temp3) {
      this.temp1 = temp1;
      if (temp1 != null) {
        this.temp1.setComoTemp1(true);
      }
      this.temp2 = temp2;
      this.temp3 = temp3;
      imprimirJanela();
    }

    // envia todo mundo e inicia os temporizadores
    public void enviar() {
      if (temp1 != null) {
        Temporizador aux = temp1.copia();
        aux.enviar();
        aux.start();
        temp1.liberar();
        temp1 = aux;
      }
      if (temp2 != null) {
        Temporizador aux = temp2.copia();
        aux.enviar();
        aux.start();
        temp2.liberar();
        temp2 = aux;
      }
      ;
      if (temp3 != null) {
        Temporizador aux = temp3.copia();
        aux.enviar();
        aux.start();
        temp3.liberar();
        temp3 = aux;
      }
    }

    public Temporizador getTemp1(){
      return temp1;
    }

    // libera o primeiro e chama o proximo temporizador
    public void deslizaJanela(Temporizador novo) {
      if (temp1 != null && !temp1.isLiberado())
        temp1.liberar(); // libera o mais antigo
      temp1 = temp2;
      if (temp1 != null)
        temp1.setComoTemp1(true); // novo temp1
      temp2 = temp3;
      temp3 = novo;
      if (temp3 != null) {
        temp3.enviar();
        temp3.start();
      }
      imprimirJanela();
    }

    // libera todos os temporizadores ativos
    public void matarTemps() {
      if (temp1 != null && !temp1.isLiberado()) {
        temp1.liberar();
      }
      if (temp2 != null && !temp2.isLiberado()) {
        temp2.liberar();
      }
      if (temp3 != null && !temp3.isLiberado()) {
        temp3.liberar();
      }
    }

    // imprime a situacao da janela
    public void imprimirJanela() {
      System.out.print("Temp 1: quadro ");
      if (temp1 != null)
        System.out.println(temp1.getQuadro());
      else
        System.out.println("null");

      System.out.print("Temp 2: quadro ");
      if (temp2 != null)
        System.out.println(temp2.getQuadro());
      else
        System.out.println("null");

      System.out.print("Temp 3: quadro ");
      if (temp3 != null)
        System.out.println(temp3.getQuadro());
      else
        System.out.println("null");
    } // fim do imprimir janela

    public void receberAckSeletivo(int numAck) {

      // Verifica Temp 1
      if (temp1 != null && temp1.getQuadro() == numAck) {
        int i=0;
        do{
          contadorTemporizadores = (contadorTemporizadores) % 10 + 1; // avanca 1
          if (contT >= temporizadores.length) {
            janelaDeslizante.deslizaJanela(null);
          } else {
            janelaDeslizante.deslizaJanela(temporizadores[contT]);
            temporizadores[contT] = null;
            contT++;
          }
          i++;
        }while(temp1==null && i<3);
      }
      // Verifica Temp 2
      else if (temp2 != null && temp2.getQuadro() == numAck) {
        System.out.println("ACK do quadro " + numAck + " recebido fora de ordem.");
        temp2.liberar(); // Apenas para o timer, nao desliza
        temp2 = null;
      }
      // Verifica Temp 3
      else if (temp3 != null && temp3.getQuadro() == numAck) {
        System.out.println("ACK do quadro " + numAck + " recebido fora de ordem.");
        temp3.liberar(); // Apenas para o timer, nao desliza
        temp3=null;
      }
    } // fim do recebeAckSeletivo

  } // fim da classe JanelaDeslizante

} // fim da classe CamadaEnlaceDadosTransmissora