/* ***************************************************************
* Autor............: Isis Caroline Lima Viana
* Matricula........: 202410016
* Inicio...........: 16/08/2025
* Ultima alteracao.: 20/11/2025
* Nome.............: AplicacaoTransmissora.java
* Funcao...........: Essa camada eh responsavel por ler o que esta 
escrito no campo de texto quando se clica no botaoEnviar/botaoScan 
e envia-la para a camada de aplicacao transmissora
*************************************************************** */
package modelo;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AplicacaoTransmissora{
  
  private TextArea campoDeTexto;
  private TextArea campoDeTexto2;
  private ImageView imagemImpressora;
  private CamadaAplicacaoTransmissora camada_Aplicacao_Transmissora;
  private boolean ehTransmissor;
  
  //Construtor do host A(computador):
  public AplicacaoTransmissora(TextArea campoDeTexto, TextArea campoDeTexto2, CamadaAplicacaoTransmissora camada_Aplicacao_Transmissora, boolean ehTransmissor){
    this.campoDeTexto = campoDeTexto;
    this.camada_Aplicacao_Transmissora = camada_Aplicacao_Transmissora;
    this.ehTransmissor = ehTransmissor;
    this.campoDeTexto2 = campoDeTexto2;
    Ponteiro.cont = 0;
  }
  //Contrutor do host B(impressora):
  public AplicacaoTransmissora(TextArea campoDeTexto, TextArea campoDeTexto2, CamadaAplicacaoTransmissora camada_Aplicacao_Transmissora, 
  ImageView imagemImpressora, boolean ehTransmissor){
    this.campoDeTexto = campoDeTexto;
    this.camada_Aplicacao_Transmissora = camada_Aplicacao_Transmissora;
    this.imagemImpressora = imagemImpressora;
    this.ehTransmissor = ehTransmissor;
    this.campoDeTexto2 = campoDeTexto2;
  }
  
  /* ***************************************************************
   * Metodo: aplicacaoTransmissora
   * Funcao: pega a mensagem do campo de texto e a envia para a 
   * camada de Aplicacao Transmissora
   * Parametros: nenhum
   * Retorno: vazio
   * ****************************************************************/
  public void aplicacaoTransmissora () {
    String mensagem;
    if(ehTransmissor)
      mensagem = campoDeTexto.getText();
    else
      mensagem = campoDeTexto2.getText();
    campoDeTexto.setDisable(true);
    campoDeTexto2.setDisable(true);
    System.out.println("Aplicacao Transmissora: "+mensagem);
    if(!ehTransmissor){
      animacaoImpressora(mensagem);
    }else{
       //chama a proxima camada:
      camada_Aplicacao_Transmissora.camadaAplicacaoTransmissora(mensagem); //em um exemplo mais realistico, aqui seria dado um SEND do SOCKET
    }
    
  }//fim do metodo AplicacaoTransmissora


  /*
   * ***************************************************************
   * Metodo: animacaoImpressora
   * Funcao: Roda a animação em uma Thread separada para não travar
   * a interface gráfica.
   * ****************************************************************/
  public void animacaoImpressora(String mensagem) {
    if (imagemImpressora == null) return;

    Thread threadAnimacao = new Thread(() -> {
      for (int i = 9; i >= 1; i--) {
        final int index = i;
        
        Platform.runLater(() -> {
            imagemImpressora.setImage(new Image("/imagens/impressora/" + index + ".png"));
        });

        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      } 
      //chama a proxima camada:
      camada_Aplicacao_Transmissora.camadaAplicacaoTransmissora(mensagem); 
      //em um exemplo mais realistico, aqui seria dado um SEND do SOCKET
    });
    
    threadAnimacao.setDaemon(true);
    threadAnimacao.start();
  } // fim do animacaoImpressora
  
  /* ***************************************************************
   * Metodo: parar
   * Funcao: pega a mensagem do campo de texto e a envia para a 
   * camada de Aplicacao Transmissora
   * Parametros: nenhum
   * Retorno: vazio
   * ****************************************************************/
  public void parar(){
    camada_Aplicacao_Transmissora.parar();  //vai chamar o metodo parar ate a camada fisica transmissora
  }

  //Envia a nova taxa de erro de camada em camada ate chegar ao meioDeComunicacao
  public void setTaxaDeErro(int taxa){
    camada_Aplicacao_Transmissora.setTaxaDeErro(taxa);
  }
  
} //fim da classe AplicacaoTransmissora