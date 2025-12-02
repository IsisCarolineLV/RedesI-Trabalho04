/* ***************************************************************
* Autor............: Isis Caroline Lima Viana
* Matricula........: 202410016
* Inicio...........: 04/06/2025
* Ultima alteracao.: 02/11/2025
* Nome.............: Principal.java
* Funcao...........: O dado projeto tem como intuito ser uma simulacao 
das camadas fisica e de enlace de dados do modelo OSI. Esta classe eh a
responsavel por criar o scene e o stage e carregar nele a interface
visual do arquivo fxml. A instanciacao das camadas e estruturacao
principal do projeto encontram-se no ControllerTela.java
*************************************************************** */
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
//import javafx.stage.StageStyle;
import controle.ControllerTela;

public class Principal extends Application{
  
  /* ***************************************************************
  * Metodo: main
  * Funcao: primeiro metodo chamado quando o progaram eh iniciado. 
  * Apenas chama o metodo launch para iniciar a aplicação
  * Parametros: argumentos da linha de comando
  * Retorno: vazio
  * *************************************************************** */
  public static void main(String[] args){
    launch();
  }

  /* ***************************************************************
  * Metodo: start
  * Funcao: eh chamado logo apos o launch(). Esse metodo eh responsavel por carregar
  * o arquivo telaPrincipal.fxml(que contem o layout da aplicacao), criar uma cena com
  * base nesse arquivo e apresentar ao usuario esse stage.
  * Parametros: stageInicial (stage principal da aplicacao)
  * Retorno: vazio
  * *************************************************************** */
  public void start(Stage stage) {
    try {
        // Carrega a interface
        Parent root = FXMLLoader.load(getClass().getResource("visao/telaPrincipal.fxml"));
        //Scene scene = new Scene(root, 878, 488); 
        Scene scene = new Scene(root);
        //stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.show();
        
    } catch (Exception e) {
        System.err.println("Falha crítica:");
        e.printStackTrace();
    }
  }
  
} //fim da classe Principal