/* ***************************************************************
* Autor............: Isis Caroline Lima Viana
* Matricula........: 202410016
* Inicio...........: 16/08/2025
* Ultima alteracao.: 23/11/2025
* Nome.............: CamadaFisicaReceptora.java
* Funcao...........: Essa camada eh responsavel por decodificar o 
sinal recebido em binario comum a partir da codificacao escolhida 
(binario, manchester ou manchester diferencial) e enviar a mensagem 
escolhida para a CamadaAplicacaoReceptora. Caso o enquadramento 
escolhido seja a violacao da camada fisica essa camada tbm se
torna responsavel por desenquadrar e decodificar a mensagem
****************************************************************/
package modelo;

public class CamadaFisicaReceptora {

  private CamadaEnlaceDadosReceptora camada_Enlace_Dados_Receptora;
  private CamadaEnlaceDadosTransmissora camada_Enlace_Dados_Transmissora;
  private int tipoDeDecodificacao;
  private int tipoDeEnquadramento;
  private int tipoDeControleDeErro;
  //private MeioDeComunicacao copiaMeioDeComunicacao;

  // Construtor
  public CamadaFisicaReceptora(CamadaEnlaceDadosReceptora camada_Enlace_Dados_Receptora, int tipoDeDecodificacao,
      int tipoDeEnquadramento, int tipoDeControleDeErro) {
    this.camada_Enlace_Dados_Receptora = camada_Enlace_Dados_Receptora;
    this.tipoDeDecodificacao = tipoDeDecodificacao;
    this.tipoDeEnquadramento = tipoDeEnquadramento;
    this.tipoDeControleDeErro=tipoDeControleDeErro;
    // fazer animacao da impressora
  }

  public void setCamadaEnlaceDadosTransmissora(CamadaEnlaceDadosTransmissora camada_Enlace_Dados_Transmissora){
    this.camada_Enlace_Dados_Transmissora = camada_Enlace_Dados_Transmissora;
  }


  /////////////////////////////////////////////////////////////////
  ////////////////////// METODOS DA ///////////////////////////////
  //////////////// CAMADA FISICA RECEPTORA ////////////////////////
  /////////////////////////////////////////////////////////////////

  /* ***************************************************************
   * Metodo: camadaFisicaReceptora
   * Funcao: verifica qual a codificacao escolhida e chama o metodo
   * equivalente a essa escolha, por fim envia o fluxoBrutoDeBits
   * codificados para a camadaEnlaceDadosReceptora ou para a 
   * camadaEnlaceDadosTransmissora(se for um ACK)
   * Parametros: int[] (mensagem codificada no tipo escolhido)
   * Retorno: vazio
   ****************************************************************/
  public void camadaFisicaReceptora(int quadro[]) {
    int fluxoBrutoDeBits[] = new int[(int) quadro.length / 2];
    switch (tipoDeDecodificacao) {
      case 0: // codificao binaria
        fluxoBrutoDeBits = camadaFisicaReceptoraDecodificacaoBinaria(quadro);
        break;
      case 1: // codificacao manchester
        if (tipoDeEnquadramento != 3) {
          fluxoBrutoDeBits = camadaFisicaReceptoraDecodificacaoManchester(quadro);
        } else {
          fluxoBrutoDeBits = violacaoDaCamadaFisica(quadro);
        }
        break;
      case 2: // codificacao manchester diferencial
        if (tipoDeEnquadramento != 3) {
          fluxoBrutoDeBits = camadaFisicaReceptoraDecodificacaoManchesterDiferencial(quadro);
        } else {
          fluxoBrutoDeBits = violacaoDaCamadaFisica(quadro);
        }
        break;
    }// fim do switch/case
     // chama proxima camada

     if(fluxoBrutoDeBits!=null){
      System.out.println("\nCamada Fisica Receptora:\n"+imprimirVetor(fluxoBrutoDeBits));

      //eh quadro:
      if((fluxoBrutoDeBits[0]&(1<<31))==0 && tipoDeControleDeErro!=3) //primeiro bit eh 0 = eh quadro
        camada_Enlace_Dados_Receptora.camadaEnlaceDadosReceptora(fluxoBrutoDeBits);
      else if(tipoDeControleDeErro==3 && (fluxoBrutoDeBits[0]&(1<<29))==0 && (fluxoBrutoDeBits[0]&(0x007FFFFF))!=0)  //terceiro bit eh 0 = eh quadro em hamming
        camada_Enlace_Dados_Receptora.camadaEnlaceDadosReceptora(fluxoBrutoDeBits);
      //eh ACK
      else if((fluxoBrutoDeBits[0]&(0x007FFFFF))==0)
        camada_Enlace_Dados_Transmissora.ACKtemporizador(fluxoBrutoDeBits);
      else 
        camada_Enlace_Dados_Receptora.mostrarBalaoDeErro(); //serve para um erro muito especifico no primeiro bit
     }
  }// fim do metodo CamadaFisicaTransmissora

  /* ***************************************************************
   * Metodo: camadaFisicaReceptoraDecodificacaoBinaria
   * Funcao: a mensagem ja esta em AscII binario, entao apenas se
   * retorna o quadro enviado como parametro
   * Parametros: int[] (mensagem codificada em binario)
   * Retorno: int[] (mensagem codificada em binario)
   ****************************************************************/
  public int[] camadaFisicaReceptoraDecodificacaoBinaria(int quadro[]) {
    return quadro;
  }// fim do metodo CamadaFisicaReceptoraDecodificacaoBinaria

  /* ***************************************************************
   * Metodo: camadaFisicaReceptoraDecodificacaoManchester
   * Funcao: verifica os bits de 2 em 2, se achar a sequencia 10
   * escreve 1 na posicao (iteracao) em que ele foi achado no Array
   * quadroDecodificado, caso leia a sequencia 11 ele identifica erro
   * e informa a camada de Enlace receptora e, assim que le 00 para
   * a decodificacao(se havia 00 por causa de erro na transmissao a 
   * mensagem fica incompleta e isso sera identificado na camada acima)
   * Parametros: int[] (mensagem codificada em manchester)
   * Retorno: int[] (mensagem codificada em binario)
   ****************************************************************/
  public int[] camadaFisicaReceptoraDecodificacaoManchester(int quadro[]) {
    int indexQuadro = 0, k;
    int tam = (quadro.length + 2 - 1) / 2; // arredonda pra cima
    int[] quadroDecodificado = new int[tam];
    //System.out.println(imprimirBinario(quadro[0]));
    for (int i = 0; i < quadroDecodificado.length; i++) {
      k = 31;
      quadroDecodificado[i] = 0; // zera todas as posicoes
      for (int j = 31; j >= 0; j--) {

        if (j == 15) {
          indexQuadro++;
          k = 31;
        } // cada quadroDecodificado[] guarda 4 caracteres
        // entao na metade k volta pro final
        if (indexQuadro >= quadro.length)
          break;
        // se o par for 10 entao escreve um 1
        if(((quadro[indexQuadro] & 1 << k) == 0) && ((quadro[indexQuadro] & 1 << (k - 1)) == 0)){
          return quadroDecodificado;
        }else if (((quadro[indexQuadro] & 1 << k) != 0) && ((quadro[indexQuadro] & 1 << (k - 1)) == 0))
          quadroDecodificado[i] = (quadroDecodificado[i] | 1 << j);
        else if (((quadro[indexQuadro] & 1 << k) != 0) && ((quadro[indexQuadro] & 1 << (k - 1)) != 0)){
          camada_Enlace_Dados_Receptora.mostrarBalaoDeErro();
          return null;
        }
        k -= 2; // verifica o proximo par
      }
      indexQuadro++;
    }
    return quadroDecodificado;
  }// fim do metodo CamadaFisicaReceptoraDecodificacaoManchester

  /* ***************************************************************
   * Metodo: CamadaFisicaReceptoraDecodificacaoManchesterDiferencial
   * Funcao: verifica os bits de 2 em 2, se ouve mudanca de um bit
   * para o outro escreve 0, se nao escreve 1. Caso leia a sequencia 
   * 11 ele identifica erro e informa a camada de Enlace receptora e, 
   * assim que le 00 para a decodificacao(se havia 00 por causa de erro 
   * na transmissao a mensagem fica incompleta e isso sera identificado 
   * na camada acima).
   * Parametros: int[] (mensagem codificada em manchester diferencial)
   * Retorno: int[] (mensagem codificada em binario)
   ****************************************************************/
  public int[] camadaFisicaReceptoraDecodificacaoManchesterDiferencial(int quadro[]) {

    boolean estadoAnterior = false; // to usando boolean pq int ia embolar tudo 0=false e 1=true
    int indexQuadro = 0, k = 31;
    int tam = (quadro.length + 2 - 1) / 2; // arredonda pra cima
    int[] quadroDecodificado = new int[tam];

    for (int i = 0; i < quadro.length; i++) {

      for (int j = 31; j >= 0; j -= 2) {
        int a = (quadro[i] & 1 << j);
        int b = (quadro[i] & 1 << (j - 1));
        if((a == 0) && (b == 0)){
          break;
        }else if ((a != 0) && (b != 0)) {
          camada_Enlace_Dados_Receptora.mostrarBalaoDeErro();
          return null;
        }
        int mascara = 1 << j;
        boolean bitNovo = (quadro[i] & mascara) != 0;

        if (estadoAnterior != bitNovo) {
          quadroDecodificado[indexQuadro] |= 1 << k;
        }
        estadoAnterior = bitNovo;
        k--;
        if (k < 0) {
          k = 31;
          indexQuadro++;
        }
      }
    }
    return quadroDecodificado;
  }// fim do CamadaFisicaReceptoraDecodificacaoManchesterDiferencial

  /* ***************************************************************
   * Metodo: violacaoDaCamadaFisica
   * Funcao: metodo de desenquadramento que envolve identificar os quadros
   * com dois pares altos (11) no inicio e no fim. Vai copiando os bits do 
   * quadro enquadrado ate terminar todo o fluxo de bits
   * Parametros: int[] quadro (enquadrado)
   * Retorno: int[] quadroDesenquadrado
   ****************************************************************/
  public int[] violacaoDaCamadaFisica(int[] quadro) {

  // Prepara vetor de saida (tamanho maximo possivel)
  int[] quadroDecodificado = new int[(quadro.length * 32) / 8]; 
  
  int contDecodificado = 0; // indice do array de saida
  int k = 31; // deslocador do bit de saida
  
  int contQuadro = 0; // indice do array de entrada
  int ponteiro = 31; // deslocador do bit de entrada
  
  // Variaveis para Manchester Diferencial
  boolean estadoAnterior = false; 
  boolean primeiroBitDiff = true;

  while (contQuadro < quadro.length) {
      // pega o primeiro bit
      int bitA = (quadro[contQuadro] >> ponteiro) & 1;
      ponteiro--;
      if (ponteiro < 0) { ponteiro = 31; contQuadro++; }

      if (contQuadro >= quadro.length) break;

      //pega o segundo bit
      int bitB = (quadro[contQuadro] >> ponteiro) & 1;
      ponteiro--;
      if (ponteiro < 0) { ponteiro = 31; contQuadro++; }

      if (bitA == 1 && bitB == 1) { // Flag (11) -> Ignora e continua
        continue;
      } 
      else if (bitA == 0 && bitB == 0) {// (00) -> final da mensagem
        break;
      }
      else {
        int bitParaEscrever = 0;

        if (tipoDeDecodificacao == 1) { //Manchester
          if (bitA == 1 && bitB == 0) {
              bitParaEscrever = 1;
          } else {
              bitParaEscrever = 0;
          }
        } 
        else if (tipoDeDecodificacao == 2) {  //Manchester diferencial
          boolean estadoAtual = (bitA == 1);
          
          if (primeiroBitDiff) {
              primeiroBitDiff = false;
              bitParaEscrever = (estadoAtual != estadoAnterior) ? 0 : 1; 
          } else {
              bitParaEscrever = (estadoAtual != estadoAnterior) ? 0 : 1;
          }
          estadoAnterior = (bitB == 1); // O estado para o proximo é o fim deste par
        }

        //Escreve o bit referente ao par lido:
        if (bitParaEscrever == 1) {
          quadroDecodificado[contDecodificado] |= (1 << k);
        }
        k--;
        if (k < 0) {
          k = 31;
          contDecodificado++;
          if (contDecodificado >= quadroDecodificado.length) break;
        }
      } //fim do else
    } //fim do loop
    return quadroDecodificado;
  } //fim da violacaoDaCamadaFisica


  ////////////////////////////////////////////////////////////////
  //////////////////////// METODOS ///////////////////////////////
  ////////////////////// AUXILIARES //////////////////////////////
  ////////////////////////////////////////////////////////////////
  public int bytesVazios(int ultimoInt) {
    int i = 0;
    for (i = 0; i < 4; i++) {
      int mascara = 0;
      mascara = 0b11111111 << (i * 8);
      if ((mascara & ultimoInt) != 0)
        break;
    }
    return i;
  }

  public int bytesVaziosManchester(int ultimoInt) {
    int i = 0;
    for (i = 0; i < 2; i++) {
      int mascara = 0;
      mascara = 0b1111111111111111 << (i * 16);
      if ((mascara & ultimoInt) != 0)
        break;
    }
    return i;
  }

  public void avisaErro(){
    camada_Enlace_Dados_Receptora.avisaErro();
  }
  

  ////////////////////////////////////////////////////////////////
  ////////////////////// METODOS DE //////////////////////////////
  ////////////////////// IMPRESSAO ///////////////////////////////
  ////////////////////////////////////////////////////////////////

  public String imprimirBinario(int teste) {
    String a = "";
    a += String.format("%32s", Integer.toBinaryString(teste)).replace(' ', '0');
    a += "\n";
    return a;
  }

  public String imprimirVetor(int[] vetor) {
    String a = "";
    for (int i : vetor) {
      a += imprimirBinario(i);
    }
    return a;
  }

} // fim da classe CamadaFisicaReceptora