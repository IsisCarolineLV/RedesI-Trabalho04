/* ***************************************************************
* Autor............: Isis Caroline Lima Viana
* Matricula........: 202410016
* Inicio...........: 16/08/2025
* Ultima alteracao.: 23/11/2025
* Nome.............: MeioDeComunicacao.java
* Funcao...........: Simula o meio fisico de transmissao de dados.
* E uma thread para permitir que a transmissao (que e demorada
* visualmente) possa ser interrompida. Gerencia a fila de 
* mensagens, a simulacao de erros e a atualizacao visual
* do sinal na interface.
*************************************************************** */
package modelo;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

//importando as bibliotecas necessarias
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ProgressBar;
import java.util.concurrent.Semaphore;

public class MeioDeComunicacao extends Thread {
  private final Queue<int[]> filaMensagens = new LinkedList<>();
  private final Queue<Boolean> filaEmissores = new LinkedList<>();
  private Image imagensSinal[] = new Image[7];
  private ImageView[] sinalMostrado;
  private ImageView[] transicoesImageViews;
  private int[] sinal;
  private int[] transicoes;
  private int[] fluxoBrutoDeBits;
  private int tipoDeCodificacao;
  private int tipoDeEnquadramento;
  private int taxaDeErro;
  private float porcentagem;
  private CamadaFisicaReceptora camada_Fisica_Receptora_A;
  private CamadaFisicaReceptora camada_Fisica_Receptora_B;
  private volatile boolean interrompido = false;
  private ProgressBar barraDeProgresso;
  private Semaphore mutex = new Semaphore(1);
  private boolean ehTransmissor = true;

  // Construtor:
  public MeioDeComunicacao(int tipoDeCodificacao, CamadaFisicaReceptora camada_Fisica_Receptora_A,
      ImageView[] sinalMostrado, ProgressBar barraDeProgresso, ImageView[] transicoesImageViews,
      int taxaDeErro, int tipoDeEnquadramento) {
    imagensSinal[0] = new Image("/imagens/0.png");
    imagensSinal[1] = new Image("/imagens/1.png");
    imagensSinal[2] = new Image("/imagens/t.png");
    imagensSinal[3] = null;
    this.tipoDeCodificacao = tipoDeCodificacao;
    sinal = new int[16];
    transicoes = new int[15];
    for (int i = 0; i < 16; i++) {
      sinal[i] = 3;
      if (i < 15)
        transicoes[i] = 3;
    }
    this.camada_Fisica_Receptora_A = camada_Fisica_Receptora_A;
    this.barraDeProgresso = barraDeProgresso;
    this.barraDeProgresso.setVisible(false);
    this.sinalMostrado = sinalMostrado;
    this.transicoesImageViews = transicoesImageViews;
    this.taxaDeErro = taxaDeErro;
    this.tipoDeEnquadramento = tipoDeEnquadramento;
    this.setDaemon(true);
  }

  public void setCamadaFisicaReceptoraB(CamadaFisicaReceptora camada_Fisica_Receptora_B) {
    this.camada_Fisica_Receptora_B = camada_Fisica_Receptora_B;
  }

  /*
   * ***************************************************************
   * Metodo: setTaxaDeErro
   * Funcao: atualiza a variavel taxaDeErro que sera usada futuramente
   * no metodo gera erro (eh protegida com semaforo porque caso a taxa
   * fosse atualizada durante a comunicacao nao daria conflito.
   * Parametros: int[] nova taxa
   * Retorno: vazio
   ****************************************************************/
  public void setTaxaDeErro(int taxaDeErro) {
    try {
      mutex.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    this.taxaDeErro = taxaDeErro;
    mutex.release();
  }

  /*
   * ***************************************************************
   * Metodo: comunicar
   * Funcao: adiciona a nova mensagem na fila, diz quem que enviou 
   * e avisa a hread que chegou coisa (caso ela esteja dormindo)
   * Parametros: int[] fluxoBrutoDeBits
   * Retorno: vazio
   ****************************************************************/
  public synchronized void comunicar(int[] fluxoBrutoDeBits, boolean ehTransmissor) {
    filaMensagens.add(fluxoBrutoDeBits);  //guarda mensagem
    barraDeProgresso.setVisible(true);
    //guarda quem que mandou pra saber a direcao da animacao:
    filaEmissores.add(ehTransmissor); 
    notify(); //acorda o meio de ele estiver esperando (wait)
  } // fim do metodo comunicar

  /* ***************************************************************
   * Metodo: run
   * Funcao: Espera ate que mensagens cheguem na fila. Quando uma 
   * mensagem chega, determina a direcao e chama o metodo runTransmissor
   * Parametros: vazio
   * Retorno: vazio
   ****************************************************************/
  public void run() {
    while (!interrompido) {

      synchronized (this) {
        while (filaMensagens.isEmpty() && !interrompido) {
          try {
            //System.out.println("z z z");
            wait(); // espera ate que alguma mensagem chegue
          } catch (InterruptedException e) {
            return; // termina se for interrompida
          }
        }
        if (interrompido)
          break;
        fluxoBrutoDeBits = filaMensagens.poll(); // pega a próxima mensagem
      }
      ehTransmissor = filaEmissores.poll();//pega quem foi o emissor dela
      runTransmissor();
    }
  } // fim do metodo run

  /*
   * ***************************************************************
   * Metodo: runTransmissor
   * Funcao: quando alguem chama start() esse metodo eh executado
   * num processo (thread) separado. Nele transferiremos cada bit do
   * transmissor para o receptor, e, a cada insercao, atualiza o deque
   * circular sinalMostrado e atualiza o sinal mostrado na tela.
   * Caso algum erro seja gerado na funcao geraErro trocara o valor
   * a ser inserido. o erro pode ser gerado uma vez por mensagem
   * (tamanho geral de todos os quadros usando num=3)
   * Parametros: vazio
   * Retorno: vazio
   ****************************************************************/
  public void runTransmissor() {
    int[] fluxoBrutoDeBitsTransmissor = fluxoBrutoDeBits;
    int[] fluxoBrutoDeBitsReceptor = new int[fluxoBrutoDeBits.length];
    int tam = fluxoBrutoDeBits.length, tamanhoByte;

    int bytesPreechidos;
    int totalBitsValidos;

    //inverte a barra para ficar mais intuitiva a animacao
    if (ehTransmissor)
      barraDeProgresso.setScaleX(1);
    else
      barraDeProgresso.setScaleX(-1);

    //calcula quantos bits serao transferidos:
    if (tipoDeCodificacao != 0) {
      tamanhoByte = 16;
      bytesPreechidos = 2 - bytesVaziosManchester(fluxoBrutoDeBits[tam - 1]);
      if (bytesPreechidos == 0) {
        System.out.println("BYTES:" + bytesPreechidos);
        bytesPreechidos = 2 - bytesVaziosManchester(fluxoBrutoDeBits[tam - 2]);
        totalBitsValidos = (tam - 2) * 32 + (bytesPreechidos * tamanhoByte);
      } else {
        totalBitsValidos = (tam - 1) * 32 + (bytesPreechidos * tamanhoByte);
      }
    } else {
      bytesPreechidos = 4 - bytesVazios(fluxoBrutoDeBits[tam - 1]);
      tamanhoByte = 8;
      totalBitsValidos = (tam - 1) * 32 + (bytesPreechidos * tamanhoByte);
    }

    int bitCont = 0;

    // System.out.println(totalBitsValidos);
    //Gera erro:
    boolean erro = false;
    int localErro = 0;
    //verifica se eh um quadro pois nao da erro em ACK
    if((fluxoBrutoDeBits[0]&(1<<31))==0 || tipoDeEnquadramento==3){
      erro = gerarErro(); // vai haver erro na transmissao desse quadro?
      int escopoDoQuadro = (totalBitsValidos - bitCont < 24) ? (totalBitsValidos - bitCont) : 24;
      if (erro && escopoDoQuadro > 0) {
        localErro = bitCont + (new Random()).nextInt(escopoDoQuadro);
        if (tipoDeEnquadramento == 3){  //se for na violacao da camada fisica
          Platform.runLater(() -> {
            if(ehTransmissor)
              camada_Fisica_Receptora_B.avisaErro();
            else
              camada_Fisica_Receptora_A.avisaErro();
          });
        } //fim do if violacao da camada
      } //fim do if do erro
    } //fim do if do quadro

    //Transferencia:
    for (int i = 0; i < tam; i++) {
      int inteiroCopia = 0;
      int mascara = 0;
      if (interrompido)
        return; // veificacao do matarThread

      for (int j = 31; j >= 0; j--) {
        bitCont++;

        if (i == tam - 1 && j <= 31 - (tamanhoByte * bytesPreechidos)) {
          // se so tem 0 a partir daqui
          break;
        } else {

          mascara = 1 << j;

          if (interrompido)
            return; // veificacao do matarThread
          else
            porcentagem = (bitCont * 100) / totalBitsValidos; // atualiza a porcentagem da barra

          boolean comparacao = (mascara & fluxoBrutoDeBitsTransmissor[i]) != 0;
          // se estamos no local que foi sorteado para dar erro e houve um erro inverte o
          // valor verdade de comparacao (ou seja, se ia colocar um 1 coloca um 0 e
          // vice-versa)
          if (erro && localErro == bitCont) {
            comparacao = !comparacao;
            System.out.println("Deu erro");
          }

          if (comparacao) {
            // coloca um 1
            inteiroCopia = inteiroCopia | mascara;
            if (ehTransmissor)
              atualizaSinalMostrado(1);
            else
              atualizaSinalMostradoReceptor(1);
          } else {
            // pula um bit para deixar um 0
            if (ehTransmissor)
              atualizaSinalMostrado(0);
            else
              atualizaSinalMostradoReceptor(0);
          } // fim if

        } // fim do else

      } // fim for interno

      fluxoBrutoDeBitsReceptor[i] = inteiroCopia;

    } // fim for externo

    // anula todo o sinal mostrado
    for (int i = 0; i < 16; i++) {
      if (ehTransmissor)
        atualizaSinalMostrado(3);
      else
        atualizaSinalMostradoReceptor(3);
    } // fim for
    // camada_Fisica_Receptora.transportaErro(deuErro);
    barraDeProgresso.setVisible(false);
    porcentagem = 0;
    if (ehTransmissor) {
      System.out.println("Transmissor enviou mensagem");
      camada_Fisica_Receptora_B.camadaFisicaReceptora(fluxoBrutoDeBitsReceptor);
    } else {
      System.out.println("Receptor enviou mensagem");
      camada_Fisica_Receptora_A.camadaFisicaReceptora(fluxoBrutoDeBitsReceptor);
    }
  } // fim do runTransmissor

  /*
   * ***************************************************************
   * Metodo: atualizaSinalMostrado
   * Funcao: coloca o novo bit no deque circular e, se ele for diferente
   * do antigo primeiro adiciona uma transicao. Depois disso, chama o
   * metodo mostrarSinalNaTela que exibira a nova disposicao do sinal
   * Parametros: bitNovo
   * Retorno: vazio
   ****************************************************************/
  public void atualizaSinalMostrado(int bitNovo) {
    if (interrompido)
      return; // veificacao do matarThread
    for (int i = sinal.length - 1; i >= 0; i--) {
      if (interrompido)
        return;
      if (i != 0) {
        sinal[i] = sinal[i - 1];
      } else {
        // primeiro elemento recebe o bit novo
        sinal[0] = bitNovo;
      } // fim do if-else
      if (i < sinal.length - 1) {
        if (sinal[i] != sinal[i + 1] && (sinal[i + 1] != 3) && (sinal[i] != 3))
          transicoes[i] = 2;
        else
          transicoes[i] = 3;
      }

    } // fim do for

    mostrarSinalNaTela();

  } // fim do metodo atualizaSinalMostrado

  /* ***************************************************************
   * Metodo: atualizaSinalMostradoReceptor
   * Funcao: Desloca o array sinal para a esquerda, insere o
   * bitNovo na ultima posicao (simulando a direcao oposta) e
   * calcula as transicoes. E chama mostrarSinalNaTela.
   * Parametros: bitNovo (int)
   * Retorno: vazio
   ****************************************************************/
  public void atualizaSinalMostradoReceptor(int bitNovo) {
    if (interrompido)
      return; // veificacao do matarThread
    for (int i = 0; i < sinal.length; i++) {
      if (interrompido)
        return;
      if (i != sinal.length - 1) {
        sinal[i] = sinal[i + 1];
      } else {
        // ultimo elemento recebe o bit novo
        sinal[sinal.length - 1] = bitNovo;
      }
      if (i > 0) {
        if (sinal[i - 1] != sinal[i] && (sinal[i - 1] != 3) && (sinal[i] != 3))
          transicoes[i - 1] = 2;
        else
          transicoes[i - 1] = 3;
      }

    } // fim do for

    mostrarSinalNaTela();
  }

  /*
   * ***************************************************************
   * Metodo: mostrarSinalNaTela
   * Funcao: atualiza as imagens dos imageViews do sinal mostrado e
   * das transicoes seguindo os deques circulares, depois dorme por
   * um tempinho para dar tempo se visualizar o sinal
   * Parametros: bitNovo
   * Retorno: vazio
   ****************************************************************/
  public void mostrarSinalNaTela() {
    if (interrompido)
      return; // veificacao do matarThread

    if (tipoDeCodificacao == 0) { // se for binario
      for (int i = 0; i < 8; i++) {
        int index = i;
        Platform.runLater(() -> {
          sinalMostrado[index * 2].setImage(imagensSinal[sinal[index]]);
          sinalMostrado[index * 2 + 1].setImage(imagensSinal[sinal[index]]);
          if ((index * 2 + 1) < 15)
            transicoesImageViews[index * 2 + 1].setImage(imagensSinal[transicoes[index]]);
          barraDeProgresso.setProgress(porcentagem / 100);
        });
      }

    } else { // se for manchester ou manchester diferencial
      for (int i = 0; i < 16; i++) {
        int index = i;
        Platform.runLater(() -> {
          sinalMostrado[index].setImage(imagensSinal[sinal[index]]);
          if (index < 15)
            transicoesImageViews[index].setImage(imagensSinal[transicoes[index]]);
          barraDeProgresso.setProgress(porcentagem / 100);
        });
      }
    } // fim else

    try {
      if (tipoDeCodificacao == 0)
        Thread.sleep(100);
      else
        Thread.sleep(70);
    } catch (InterruptedException e) {
      return; // sai do metodo se a thread foi interrompida
    }
  } // fim do metodo mostrarSinalNaTela

  
  ////////////////////////////////////////////////////////////////
  //////////////////////// METODOS ///////////////////////////////
  ////////////////////// AUXILIARES //////////////////////////////
  ////////////////////////////////////////////////////////////////

  /*
   * ***************************************************************
   * Metodo: gerarErro
   * Funcao: gera um numero aleatorio de 0 a 100, se esse numero for
   * menor que a taxa de erro (retorna true)
   * Parametros: nenhum
   * Retorno: boolean (se houve erro ou nao)
   ****************************************************************/
  public boolean gerarErro() {
    try {
      mutex.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    int aux = taxaDeErro;
    mutex.release();

    if (aux == 0)
      return false;

    Random gerador = new Random();
    int num = gerador.nextInt(100) + 1;
    // System.out.println(num);
    return num <= aux;
  } // fim do metodo gerarErro

  // conta quantos bytes vazios tem no inteiro
  public int bytesVazios(int ultimoInt) {
    int i = 0;
    for (i = 0; i < 4; i++) {
      int mascara = 0;
      mascara = 0b11111111 << (i * 8);
      if ((mascara & ultimoInt) != 0)
        break;
    }
    return i;
  } // fim do metodo bytesVazios

  // conta quantos bytes vazios tem no inteiro quando a
  // codificacao eh Manchester ou Manchester Diferencial
  public int bytesVaziosManchester(int ultimoInt) {
    int i = 0;
    for (i = 0; i < 2; i++) {
      int mascara = 0;
      mascara = 0b1111111111111111 << (i * 16);
      if ((mascara & ultimoInt) != 0)
        break;
    }
    return i;
  } // fim do metodo bytesVaziosManchester

  /*
   * ***************************************************************
   * Metodo: matarThread
   * Funcao: volta os elementos(barra de progresso e imageViews) para
   * os seus respectivos estados iniciais e interrompe a thread por
   * meio da variavel interrompido (que eh verificada em varios locais
   * distintos do codigo)
   * Parametros: nenhum
   * Retorno: vazio
   ****************************************************************/
  public void matarThread() {
    filaMensagens.clear();
    filaEmissores.clear();
    barraDeProgresso.setProgress(0);
    barraDeProgresso.setVisible(false);
    for (int s = 0; s < sinal.length; s++) {
      int index = s;
      Platform.runLater(() -> {
        sinalMostrado[index].setImage(imagensSinal[3]);
        if (index < 15)
          transicoesImageViews[index].setImage(imagensSinal[3]);
      });
    }
    interrompido = true;
    // this.interrupt(); // acorda a thread se estiver dormindo
  } // fim do metodo matarThread

  public void imprimirBinario(int teste) {
    for (int i = 31; i >= 0; i--) { // do bit mais alto para o mais baixo
      int mascara = 1 << i;
      if ((teste & mascara) != 0) {
        System.out.print("1");
      } else {
        System.out.print("0");
      }
    }
    System.out.println(); // quebra de linha no final
  }

} // fim da classe MeioDeComunicacao