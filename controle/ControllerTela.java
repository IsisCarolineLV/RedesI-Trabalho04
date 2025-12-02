/* ***************************************************************
* Autor............: Isis Caroline Lima Viana
* Matricula........: 202410016
* Inicio...........: 16/08/2025
* Ultima alteracao.: 20/11/2025
* Nome.............: ControllerTela.java
* Funcao...........: Essa classe controla a interface javaFX, controlando
a visibilidade dos panes, definindo os metodos que reagirao aos eventos do 
botoes e criando a referencia para cada elemento da interface.Alem disso,
nessa classe sao instanciados cada uma das camadas envolvidas nessa 
simulacao. 
*************************************************************** */
package controle;

//importando as bibliotecas necessarias
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.QuadCurve;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import modelo.*;

public class ControllerTela {

  @FXML
  private QuadCurve seta1;

  @FXML
  private QuadCurve seta2;

  @FXML
  private QuadCurve seta3;

  @FXML
  private Text textoErros;

  @FXML
  private Label labelEnquadramento;

  @FXML
  private Label labelCodificacao;

  @FXML
  private Label labelControleErro;

  @FXML
  private Label labelControleFluxo;

  @FXML
  private Label labelCliqueCanetas; //recado secreto

  @FXML
  private Label labelERRO; // para mostrar a taxa de erro na tela

  @FXML
  private Slider sliderErro; // para definir a taxa de erro na mensagem

  @FXML
  private ProgressBar barraDeProgresso;

  // Botoes:
  @FXML
  private Button botaoScan;

  @FXML
  private Button botaoEscrever;

  @FXML
  private Button botaoEnviar;

  @FXML
  private Button botaoSair;

  @FXML
  private Button botaoSairMenu;

  @FXML
  private Button botaoSairFolha; // esconde o paneFolha

  @FXML
  private Button botaoVer; // mostra o paneFolha

  @FXML
  private Button botaoVoltar; // retorna para o menu

  @FXML
  private Button botaoIniciar; // esconde o menu e
                               // inicia a simulacao

  // Panes:
  @FXML
  private AnchorPane paneFolha; // onde mostra o texto decodificado

  @FXML
  private AnchorPane paneMenu;

  @FXML
  private AnchorPane panePrincipal;

  @FXML
  private AnchorPane paneSinal; // mostra o sinal

  // Elementos de texto:
  @FXML
  private TextArea campoDeTexto;// onde pode-se digitar a mensagem

  @FXML
  private TextArea campoDeTexto2;// onde pode-se digitar a mensagem na folha

  @FXML
  private Text textoDecodificado;// onde a mensagem decodificada sera exibida

  // ComboBox:
  @FXML
  private ComboBox<String> comboBox; // para selecionar o tipo de codificacao

  @FXML
  private ComboBox<String> comboBoxEnquadramento; // para selecionar o tipo de enquadramento

  @FXML
  private ComboBox<String> comboBoxControle; // para selecionar o tipo de controle de erro

  @FXML
  private ComboBox<String> comboBoxControleFluxo; // para selecionar o tipo de controle de fluxo

  // ImageViews:
  @FXML
  private ImageView imagemCanetas;

  @FXML
  private ImageView imagemBalaoErro;

  @FXML
  private ImageView imagemBalaoErroPC;

  @FXML
  private ImageView imagemFolha; // a mensagem decodificada eh exibida em cima dessa imagem

  @FXML
  private ImageView imagemFundo;

  @FXML
  private ImageView imagemImpressora;

  @FXML
  private ImageView imagemMenu; // fundo do menu

  // ImageViews do sinal binario:
  @FXML
  private ImageView imagemSinal1;

  @FXML
  private ImageView imagemSinal2;

  @FXML
  private ImageView imagemSinal3;

  @FXML
  private ImageView imagemSinal4;

  @FXML
  private ImageView imagemSinal5;

  @FXML
  private ImageView imagemSinal6;

  @FXML
  private ImageView imagemSinal7;

  @FXML
  private ImageView imagemSinal8;

  @FXML
  private ImageView imagemSinal9;

  @FXML
  private ImageView imagemSinal10;

  @FXML
  private ImageView imagemSinal11;

  @FXML
  private ImageView imagemSinal12;

  @FXML
  private ImageView imagemSinal13;

  @FXML
  private ImageView imagemSinal14;

  @FXML
  private ImageView imagemSinal15;

  @FXML
  private ImageView imagemSinal16;

  @FXML
  private ImageView imagemTransicao1;

  @FXML
  private ImageView imagemTransicao2;

  @FXML
  private ImageView imagemTransicao3;

  @FXML
  private ImageView imagemTransicao4;

  @FXML
  private ImageView imagemTransicao5;

  @FXML
  private ImageView imagemTransicao6;

  @FXML
  private ImageView imagemTransicao7;

  @FXML
  private ImageView imagemTransicao8;

  @FXML
  private ImageView imagemTransicao9;

  @FXML
  private ImageView imagemTransicao10;

  @FXML
  private ImageView imagemTransicao11;

  @FXML
  private ImageView imagemTransicao12;

  @FXML
  private ImageView imagemTransicao13;

  @FXML
  private ImageView imagemTransicao14;

  @FXML
  private ImageView imagemTransicao15;

  // Cria as images que serao exibidas nos imageViews logo no comeco
  // para que nao seja preciso instancia-las muitas e muitas vezes:
  private Image[] imagens = new Image[12];
  // Arrays dos imageViews dos sinais (para facilitar sua manipulacao)
  private ImageView[] imagensDoSinal;
  private ImageView[] imagensTransicoes;

  private int tipoDeCodificacao = 5; // salva a codificacao escolhida no comboBox
  private int tipoDeEnquadramento = 5; // salva o enquadramento escolhido
  private int tipoDeControleDeErro = 5;
  private int tipoDeControleDeFluxo = 5;
  private int taxaDeErro = 0;

  private Host hostA; //COMPUTADOR
  private Host hostB; //IMPRESSORA

  /*
   * ***************************************************************
   * Metodo: initialize
   * Funcao: chama os metodos para todos os recursos necessarios para
   * que a logica do programa (os arrays de imagem, os listeners do
   * campo de texto e do comboBox, estilizar a barra de progresso,etc)
   * Parametros: nenhum
   * Retorno: vazio
   ****************************************************************/
  public void initialize() {
    criarArraysDeImagens();
    botaoEnviar.setDisable(true); // enquanto nada estiver escrito o botao fica desabilitado
    criarListeners();
    barraDeProgresso.setStyle("-fx-accent: #80d9a8;"); // pintando a barra de verde
    campoDeTexto.setText(""); // limpando o campoDeTexto
    //pintando o campo da folha da cor da pagina
    campoDeTexto2.setStyle("-fx-control-inner-background: #eae5d9;"+"-fx-border-color: transparent;");
  } // fim do initialize

  /*
   * ***************************************************************
   * Metodo: criarArraysDeImagens
   * Funcao: coloca as imagens e imageViews em seus respectivos arrays
   * Parametros: nenhum
   * Retorno: vazio
   ****************************************************************/
  public void criarArraysDeImagens() {
    // Array de imagens:
    imagens[0] = new Image("/imagens/tela1.png");
    imagens[1] = new Image("/imagens/telaSair.png");
    imagens[2] = new Image("/imagens/telaVoltar.png");
    imagens[3] = new Image("/imagens/telaMenu.png");
    imagens[4] = new Image("/imagens/telaMenu.png");
    imagens[5] = new Image("/imagens/telaMenuSair.png");
    imagens[6] = new Image("/imagens/folha.png");
    imagens[7] = new Image("/imagens/folhaSair.png");
    imagens[8] = new Image("/imagens/impressora/verPagina.png");
    imagens[9] = new Image("/imagens/impressora/1.png");
    imagens[10] = new Image("/imagens/botaoEscrever.png");
    imagens[11] = new Image("/imagens/botaoEscreverDisable.png");

    // Array de imageView dos sinais:
    imagensDoSinal = new ImageView[] { imagemSinal1, imagemSinal2, imagemSinal3,
        imagemSinal4, imagemSinal5, imagemSinal6,
        imagemSinal7, imagemSinal8, imagemSinal9,
        imagemSinal10, imagemSinal11, imagemSinal12,
        imagemSinal13, imagemSinal14, imagemSinal15,
        imagemSinal16 };

    // Array de imageView das transicoes:
    imagensTransicoes = new ImageView[] { imagemTransicao1, imagemTransicao2, imagemTransicao3,
        imagemTransicao4, imagemTransicao5, imagemTransicao6,
        imagemTransicao7, imagemTransicao8, imagemTransicao9,
        imagemTransicao10, imagemTransicao11, imagemTransicao12,
        imagemTransicao13, imagemTransicao14, imagemTransicao15 };

  } // fim do criarArraysDeImagens

  /*
   * ***************************************************************
   * Metodo: criarListeners
   * Funcao: cria os listeners para o campo de texto e para o comboBox
   * Parametros: nenhum
   * Retorno: vazio
   ****************************************************************/
  public void criarListeners() {
    // listener para ativar o botao enviar quando houver algo escrito no campo:
    campoDeTexto.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String textoAntigo, String textoNovo) {
        botaoEnviar.setDisable(textoNovo.equals("") || textoNovo == null);
        paneFolha.setVisible(false);
      }
    });

    // listener para limitar o tamanho da mensagem para que ela nao ultrapasse o 12
    // linhas:
    campoDeTexto.setTextFormatter(new TextFormatter<String>(change -> {
      if (change.isContentChange()) {
        campoDeTexto.setStyle("-fx-background-color: #fddaff;");
        String novoTexto = change.getControlNewText(); // retorna o texto
        int quebrasDeLinha = (int) (novoTexto.chars().filter(ch -> ch == '\n').count() + 1); // verifica quantas quebras
                                                                                             // de linha teve
        quebrasDeLinha += (int) (novoTexto.length() / 12);
        if (novoTexto.length() > 180 || quebrasDeLinha > 13) {
          return null; // ignora a digitação extra
        }
      }
      return change;
    }));

    // setando as opcoes do comboBox de codificacoes
    comboBox.getItems().addAll("Binario",
        "Manchester",
        "Manchester Diferencial");
    // criando o listener do comboBox de codificacao
    comboBox.setOnAction(e -> {
      String selecionado = comboBox.getValue();
      labelCodificacao.setText(selecionado);
      // salva o valor lido na variavel tipoDeCodificacao:
      tipoDeCodificacao = comboBox.getItems().indexOf(selecionado);
      if (tipoDeEnquadramento == 3 && tipoDeCodificacao == 0)
        botaoIniciar.setDisable(true);
      else if (comboBoxEnquadramento.getValue() != null && comboBoxControle.getValue() != null
          && comboBoxControleFluxo.getValue() != null)
        botaoIniciar.setDisable(false);
    }); // fim do listener

    // setando as opcoes do comboBox de enquadramento
    comboBoxEnquadramento.getItems().addAll("Contagem de Caracteres",
        "Insercao de Bytes",
        "Insercao de Bits",
        "Violacao da Camada Fisica");
    // criando o listener do comboBox de enquadramento
    comboBoxEnquadramento.setOnAction(e -> {
      String selecionado = comboBoxEnquadramento.getValue();
      labelEnquadramento.setText(selecionado);
      // salva o valor lido na variavel tipoDeEnquadramento:
      tipoDeEnquadramento = comboBoxEnquadramento.getItems().indexOf(selecionado);
      if (tipoDeEnquadramento == 3 && tipoDeCodificacao == 0)
        botaoIniciar.setDisable(true);
      else if (comboBox.getValue() != null && comboBoxControle.getValue() != null
          && comboBoxControleFluxo.getValue() != null)
        botaoIniciar.setDisable(false);
    }); // fim do listener

    // setando as opcoes do comboBox de Controle de Erros
    comboBoxControle.getItems().addAll("Paridade Par",
        "Paridade Impar",
        "CRC",
        "Codigo de Hamming");
    // criando o listener do comboBox de Controle de Erros
    comboBoxControle.setOnAction(e -> {
      String selecionado = comboBoxControle.getValue();
      labelControleErro.setText(selecionado);
      // salva o valor lido na variavel tipoDeCodificacao:
      tipoDeControleDeErro = comboBoxControle.getItems().indexOf(selecionado);
      if (tipoDeCodificacao == 0 && tipoDeEnquadramento == 3)
        botaoIniciar.setDisable(true);
      else if (comboBoxEnquadramento.getValue() != null && comboBox.getValue() != null
          && comboBoxControleFluxo.getValue() != null)
        botaoIniciar.setDisable(false);
    }); // fim do listener

    // setando as opcoes do comboBox de Controle de Fluxo
    comboBoxControleFluxo.getItems().addAll("Um bit",
        "Go-back-N",
        "Com retransmissao seletiva");
    // criando o listener do comboBox de Controle de Fluxo
    comboBoxControleFluxo.setOnAction(e -> {
      String selecionado = comboBoxControleFluxo.getValue();
      labelControleFluxo.setText(selecionado);
      // salva o valor lido na variavel tipoDeControleDeFluxo:
      tipoDeControleDeFluxo = comboBoxControleFluxo.getItems().indexOf(selecionado);
      if (tipoDeCodificacao == 0 && tipoDeEnquadramento == 3)
        botaoIniciar.setDisable(true);
      else if (comboBoxEnquadramento.getValue() != null && comboBox.getValue() != null
          && comboBoxControle.getValue() != null)
        botaoIniciar.setDisable(false);
    }); // fim do listener

    //listener do slider da taxa de erro
    sliderErro.valueProperty().addListener((observable, oldValue, newValue) -> {
      taxaDeErro = (int) Math.round(newValue.doubleValue()) * 10;
      labelERRO.setText(taxaDeErro + "%");
      hostA.setTaxaDeErro(taxaDeErro);
      hostB.setTaxaDeErro(taxaDeErro);
    });

    //listener da propriedade disable, enquanto estiver desabilitado quero que algumas
    //coisas da GUI sumam
    botaoEscrever.disabledProperty().addListener((obs, oldVal, taTravado) -> {
      if(taTravado){
        imagemCanetas.setImage(imagens[11]);
        seta1.setVisible(false);
        seta2.setVisible(false);
        seta3.setVisible(false);
        labelCliqueCanetas.setVisible(false);
      }else{
        imagemCanetas.setImage(imagens[10]);
        seta1.setVisible(true);
        seta2.setVisible(true);
        seta3.setVisible(true);
        labelCliqueCanetas.setVisible(true);
      }
    });

  } // fim do criarListeners

  /*
   * ***************************************************************
   * Metodo: criarCamadas
   * Funcao: instancia as camadas e relaciona-as umas com as outras
   * Parametros: nenhum
   * Retorno: vazio
   ****************************************************************/
  public void criarCamadas() {
    //Vai criar as camadas receptoras do computador (host A)
    hostA = new Host( botaoEnviar, botaoEscrever,campoDeTexto, campoDeTexto2, tipoDeEnquadramento,
      imagemBalaoErroPC, tipoDeControleDeErro, tipoDeCodificacao, tipoDeControleDeFluxo);

    //Vai criar as camadas receptoras da impressora (host B)
    hostB = new Host(imagemImpressora, botaoEnviar, botaoEscrever,
        textoDecodificado, campoDeTexto, campoDeTexto2, botaoVer, textoErros, tipoDeEnquadramento, imagemBalaoErro,
        tipoDeControleDeErro, tipoDeCodificacao, tipoDeControleDeFluxo);

    //Instancia o meio de comunicacao ligando as camadas receptoras do A e do B
    MeioDeComunicacao meioDeComunicacao = new MeioDeComunicacao(tipoDeCodificacao,
        hostA.getCamadaFisicaReceptora(), imagensDoSinal, barraDeProgresso, imagensTransicoes,
        taxaDeErro, tipoDeEnquadramento);
    meioDeComunicacao.setCamadaFisicaReceptoraB(hostB.getCamadaFisicaReceptora());

    //Vai criar as camadas transmissoras do computador (host A)
    hostA.setTransmissao(meioDeComunicacao, tipoDeCodificacao, tipoDeEnquadramento, 
    tipoDeControleDeErro,tipoDeControleDeFluxo, campoDeTexto, campoDeTexto2);
    //Vai criar as camadas transmissoras da impressora (host B)
    hostB.setTransmissao(meioDeComunicacao, tipoDeCodificacao, tipoDeEnquadramento,
    tipoDeControleDeErro,tipoDeControleDeFluxo,campoDeTexto, campoDeTexto2, imagemImpressora);

    //inicia o meio de comunicacao para ele ficar esperando mensagens
    meioDeComunicacao.start();
  } // fim do criarCamadas

  ///////// METODOS QUE RESPONDEM AOS EVENTOS DOS BOTOES /////////////
  
  /*
   * ***************************************************************
   * Metodo: clicouEnviar
   * Funcao: desabilita os botoes de enviar e ver a mensagem, esconde
   * o paneFolha e chama a aplicacaoTransmissora
   * Parametros: ActionEvent (botao clicado)
   * Retorno: vazio
   ****************************************************************/
  @FXML
  void clicouEnviar(ActionEvent event) {
    paneFolha.setVisible(false);
    botaoEnviar.setDisable(true);
    botaoEscrever.setDisable(true);
    botaoVer.setDisable(true);
    hostA.enviar();
    hostB.setErro(false);
    // sliderErro.setDisable(true);
  } // fim do clicouEnviar

  /*
   * ***************************************************************
   * Metodo: clicouScan
   * Funcao: desabilita os botoes de enviar e ver a mensagem, esconde
   * o paneFolha e chama a aplicacaoTransmissora
   * Parametros: ActionEvent (botao clicado)
   * Retorno: vazio
   ****************************************************************/
  @FXML
  void clicouScan(ActionEvent event) {
    paneFolha.setVisible(false);
    botaoEnviar.setDisable(true);
    botaoEscrever.setDisable(true);
    botaoVer.setDisable(true);
    hostA.setErro(false);
    //precisei dessa pausa para que a folha sumisse antes de enviar a mensagem
    PauseTransition pause = new PauseTransition(Duration.millis(100)); // 0.1 segundo
    pause.setOnFinished(e -> {  //quando a pausa acabar envia
        hostB.enviar(); 
    });
    pause.play();
  }
  /*
   * ***************************************************************
   * Metodo: clicouSair
   * Funcao: fecha a aplicacao
   * Parametros: ActionEvent (botao clicado)
   * Retorno: vazio
   ****************************************************************/
  @FXML
  void clicouSair(ActionEvent event) {
    Platform.exit(); // fecha a aplicacao
  } // fim do clicouSair

  /*
   * ***************************************************************
   * Metodo: clicouVoltar
   * Funcao: mata a Thread e retorna todos os elementos para seus estados
   * iniciais e deixa o paneMenu visivel novamente
   * Parametros: ActionEvent (botao clicado)
   * Retorno: vazio
   ****************************************************************/
  @FXML
  void clicouVoltar(ActionEvent event) {
    hostB.parar();
    hostA.parar();
    paneMenu.setVisible(true);
    campoDeTexto.setText("");
    campoDeTexto2.setText("");
    botaoVer.setDisable(true);
    botaoEnviar.setDisable(false);
    botaoEscrever.setDisable(false);
    campoDeTexto.setDisable(false);
    campoDeTexto2.setDisable(false);
    textoDecodificado.setText("");
    paneFolha.setVisible(false);
    sliderErro.setDisable(false);
    sliderErro.setValue(0);
    hostA.setErro(false);
    hostB.setErro(false);
  } // fim do clicouVoltar

  /*
   * ***************************************************************
   * Metodo: clicouSairFolha
   * Funcao: esconde o paneFolha
   * Parametros: ActionEvent (botao clicado)
   * Retorno: vazio
   ****************************************************************/
  @FXML
  void clicouSairFolha(ActionEvent event) {
    paneFolha.setVisible(false);
  } // fim do clicouSairFolha

  /*
   * ***************************************************************
   * Metodo: clicouVerFolha
   * Funcao: mostra o paneFolha
   * Parametros: ActionEvent (botao clicado)
   * Retorno: vazio
   ****************************************************************/
  @FXML
  void clicouVerFolha(ActionEvent event) {
    textoDecodificado.setVisible(true);
    campoDeTexto2.setVisible(false);
    botaoScan.setVisible(false);
    paneFolha.setVisible(true);
  } // fim do clicouVerFolha

  /*
   * ***************************************************************
   * Metodo: clicouIniciar
   * Funcao: esconde o paneMenu e instancia as camadas
   * Parametros: ActionEvent (botao clicado)
   * Retorno: vazio
   ****************************************************************/
  @FXML
  void clicouIniciar(ActionEvent event) {
    paneMenu.setVisible(false);
    criarCamadas();
  } // fim do clicouIniciar

  ////////////////// METODOS PURAMENTE ESTETICOS ///////////////////

  // Esse metodo aumenta o tamanho do botao para sinalizar que ele eh clicavel
  @FXML
  void destacarBotao(MouseEvent event) {
    Button botao = (Button) event.getSource(); // referencia qual botao clicou
    if (botao.getId().equals("botaoSair")) {
      imagemFundo.setImage(imagens[1]);
    } else if (botao.getId().equals("botaoVoltar")) {
      imagemFundo.setImage(imagens[2]);
    } else if (botao.getId().equals("botaoEnviar")) {
      botaoEnviar.setScaleX(1.1);
      botaoEnviar.setScaleY(1.1);
    } else if (botao.getId().equals("botaoVer")) {
      imagemImpressora.setImage(imagens[8]);
    } else if (botao.getId().equals("botaoSairMenu")) {
      imagemMenu.setImage(imagens[5]);
    } else if (botao.getId().equals("botaoSairFolha")) {
      imagemFolha.setImage(imagens[7]);
    } else if (botao.getId().equals("botaoIniciar")) {
      botaoIniciar.setScaleX(1.1);
      botaoIniciar.setScaleY(1.1);
    } else if(botao.getId().equals("botaoEscrever")){
      imagemCanetas.setScaleX(1.1);
      imagemCanetas.setScaleY(1.1);
    } else if(botao.getId().equals("botaoScan")){
      botaoScan.setScaleX(1.1);
      botaoScan.setScaleY(1.1);
    }
  } // fim do destacarBotao

  // retorna o botao destacado para seu tamanho normal
  @FXML
  void voltaBotao(MouseEvent event) {
    Button botao = (Button) event.getSource(); // referencia qual botao gerou o evento
    if (botao.getId().equals("botaoEnviar")) {
      botaoEnviar.setScaleX(1);
      botaoEnviar.setScaleY(1);
    } else if (botao.getId().equals("botaoIniciar")) {
      botaoIniciar.setScaleX(1);
      botaoIniciar.setScaleY(1);
    } else if (botao.getId().equals("botaoVer")) {
      imagemImpressora.setImage(imagens[9]);
    } else if (botao.getId().equals("botaoSairMenu")) {
      imagemMenu.setImage(imagens[3]);
    } else if (botao.getId().equals("botaoSairFolha")) {
      imagemFolha.setImage(imagens[6]);
    } else if(botao.getId().equals("botaoEscrever")){
      imagemCanetas.setScaleX(1);
      imagemCanetas.setScaleY(1);
    }else if(botao.getId().equals("botaoScan")){
      botaoScan.setScaleX(1);
      botaoScan.setScaleY(1);
    }else {
      imagemFundo.setImage(imagens[0]);
    }
  } // fim do voltaBotao

  //Mostra a folha com o campoDeTexto2 para que o usuario possa escrever e scanear
  @FXML
  void clicouEscrever(ActionEvent event) {
    textoErros.setVisible(false);
    textoDecodificado.setVisible(false);
    campoDeTexto2.setVisible(true);
    botaoScan.setVisible(true);
    paneFolha.setVisible(true);
  }

} // fim da classe ControllerTela
