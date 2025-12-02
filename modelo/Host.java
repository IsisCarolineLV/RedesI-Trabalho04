/* ***************************************************************
* Autor............: Isis Caroline Lima Viana
* Matricula........: 202410016
* Inicio...........: 16/08/2025
* Ultima alteracao.: 23/11/2025
* Nome.............: Host.java
* Funcao...........: Essa classe serve para englobar todas as camadas
num unico objeto possibilitando que elas sejam instanciadas e 
relacionadas de forma mais organica e organizada. Quando estava 
instanciando elas e as deixando soltas no controller estava muito confuso
entao agora temos um host que so com os contrutores gera todas as camadas
automaticamente (e a parte confusa ficou num lugar so)
*************************************************************** */
package modelo;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class Host {
  //
  private AplicacaoTransmissora aplicacao_Transmissora;
  private AplicacaoReceptora aplicacao_Receptora;
  // 
  private CamadaAplicacaoTransmissora camada_Aplicacao_Transmissora;
  private CamadaAplicacaoReceptora camada_Aplicacao_Receptora;
  //
  private CamadaEnlaceDadosTransmissora camada_Enlace_Dados_Transmissora;
  private CamadaEnlaceDadosReceptora camada_Enlace_Dados_Receptora;
  //
  private CamadaFisicaTransmissora camada_Fisica_Transmissora;
  private CamadaFisicaReceptora camada_Fisica_Receptora;
  //
  private boolean ehTransmissor;

  //construtor Camadas receptoras B:
  public Host(ImageView imagemImpressora, Button botaoEnviar, Button botaoScan, Text textoDecodificado,
      TextArea campoDeTexto,TextArea campoDeTexto2, Button botaoVer, Text textoErro, int tipoDeEnquadramento,
      ImageView balaoErro, int tipoDeControleDeErro, int tipoDeCodificacao, int tipoDeControleDeFluxo) {
    aplicacao_Receptora = new AplicacaoReceptora(imagemImpressora, botaoEnviar, botaoScan, textoDecodificado, 
        campoDeTexto, campoDeTexto2, botaoVer, textoErro, false);
    camada_Aplicacao_Receptora = new CamadaAplicacaoReceptora(aplicacao_Receptora);
    camada_Enlace_Dados_Receptora = new CamadaEnlaceDadosReceptora(camada_Aplicacao_Receptora, tipoDeEnquadramento,
        balaoErro, tipoDeControleDeErro, tipoDeCodificacao, tipoDeControleDeFluxo);
    camada_Fisica_Receptora = new CamadaFisicaReceptora(camada_Enlace_Dados_Receptora, tipoDeCodificacao,
        tipoDeEnquadramento, tipoDeControleDeErro);
    this.ehTransmissor = true;
  }

  //construtor Camadas transmissoras A:
  public void setTransmissao(MeioDeComunicacao meioDeComunicacao, int tipoDeCodificacao, int tipoDeEnquadramento,
      int tipoDeControleDeErro, int tipoDeControleDeFluxo, TextArea campoDeTexto, TextArea campoDeTexto2) {
    camada_Fisica_Transmissora = new CamadaFisicaTransmissora(meioDeComunicacao, tipoDeCodificacao, tipoDeEnquadramento,
        true);
    camada_Enlace_Dados_Transmissora = new CamadaEnlaceDadosTransmissora(camada_Fisica_Transmissora,tipoDeCodificacao,
        tipoDeEnquadramento, tipoDeControleDeErro, tipoDeControleDeFluxo);
    camada_Aplicacao_Transmissora = new CamadaAplicacaoTransmissora(camada_Enlace_Dados_Transmissora);
    aplicacao_Transmissora = new AplicacaoTransmissora(campoDeTexto,campoDeTexto2, camada_Aplicacao_Transmissora, true);

    setVolta();
  }

  //construtor Camadas receptoras A:
  public Host(Button botaoEnviar, Button botaoScan, TextArea campoDeTexto, TextArea campoDeTexto2, int tipoDeEnquadramento, 
  ImageView balaoErro, int tipoDeControleDeErro, int tipoDeCodificacao, int tipoDeControleDeFluxo){
    this.ehTransmissor = false;
    aplicacao_Receptora = new AplicacaoReceptora(botaoEnviar, botaoScan, campoDeTexto, campoDeTexto2, true);
    camada_Aplicacao_Receptora = new CamadaAplicacaoReceptora(aplicacao_Receptora);
    camada_Enlace_Dados_Receptora = new CamadaEnlaceDadosReceptora(camada_Aplicacao_Receptora, tipoDeEnquadramento, balaoErro, 
    tipoDeControleDeErro, tipoDeCodificacao, tipoDeControleDeFluxo);
    camada_Fisica_Receptora = new CamadaFisicaReceptora(camada_Enlace_Dados_Receptora, tipoDeCodificacao, tipoDeEnquadramento,
    tipoDeControleDeErro);
  }

  //construtor Camadas transmissoras B:
  public void setTransmissao(MeioDeComunicacao meioDeComunicacao, int tipoDeCodificacao, int tipoDeEnquadramento,
      int tipoDeControleDeErro, int tipoDeControleDeFluxo, TextArea campoDeTexto, TextArea campoDeTexto2, ImageView imagemImpressora) {
    camada_Fisica_Transmissora = new CamadaFisicaTransmissora(meioDeComunicacao, tipoDeCodificacao, tipoDeEnquadramento,
        false);
    camada_Enlace_Dados_Transmissora = new CamadaEnlaceDadosTransmissora(camada_Fisica_Transmissora,tipoDeCodificacao,
        tipoDeEnquadramento, tipoDeControleDeErro, tipoDeControleDeFluxo);
    camada_Aplicacao_Transmissora = new CamadaAplicacaoTransmissora(camada_Enlace_Dados_Transmissora);
    aplicacao_Transmissora = new AplicacaoTransmissora(campoDeTexto,campoDeTexto2, camada_Aplicacao_Transmissora,
      imagemImpressora, false);

    setVolta();
  }

  //Algumas camadas sao referenciadas por camadas que nao existiam no momento em que foram instanciadas
  //por isso aqui vamos setar essas referencias
  public void setVolta() {
    aplicacao_Receptora.setCamadaAplicacaoReceptora(camada_Aplicacao_Receptora);
    camada_Aplicacao_Receptora.setCamadaEnlaceDadosReceptora(camada_Enlace_Dados_Receptora);
    camada_Enlace_Dados_Receptora.setCamadaFisicaTransmissora(camada_Fisica_Transmissora);
    camada_Fisica_Receptora.setCamadaEnlaceDadosTransmissora(camada_Enlace_Dados_Transmissora);
  }

  //usados para se criar o meioDeComunicacao
  public CamadaFisicaReceptora getCamadaFisicaReceptora() {
    return camada_Fisica_Receptora;
  }

  public CamadaFisicaTransmissora getCamada_Fisica_Transmissora() {
    return camada_Fisica_Transmissora;
  }

  //chama o metodo parar das camadas de aplicacao e elas iram levando esse
  //comando para todas as seguintes e limpando tudo o que precisam no caminho
  public void parar() {
    aplicacao_Transmissora.parar();
    aplicacao_Receptora.parar();
  }

  //para iniciar o erro como falso
  public void setErro(boolean valor) {
    aplicacao_Receptora.setErro(valor);
  }

  //enviar a mensagem
  public void enviar() {
    aplicacao_Transmissora.aplicacaoTransmissora();
  }

  //envia a nova taxa de erro ate que ela chegue ao meio de comunicacao
  public void setTaxaDeErro (int taxaDeErro){
    aplicacao_Transmissora.setTaxaDeErro(taxaDeErro);
  }

  //para auxiliar nas impressoes no console
  public String getId(){
    if(ehTransmissor)
      return "Transmissor";
    else
      return "Receptor";
  }
}
