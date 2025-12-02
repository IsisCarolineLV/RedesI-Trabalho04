/* ***************************************************************
* Autor............: Isis Caroline Lima Viana
* Matricula........: 202410016
* Inicio...........: 16/08/2025
* Ultima alteracao.: 29/08/2025
* Nome.............: CamadaAplicacaoReceptora.java
* Funcao...........: Essa camada eh responsavel por decodificar a 
mensagem, transformando cada 8 bits de cada inteiro da mensagem em
seu caracter da tabela AscII e juntando-os numa unica mensagem 
*************************************************************** */
package modelo;

public class CamadaAplicacaoReceptora {

  private AplicacaoReceptora aplicacao_Receptora;
  private CamadaEnlaceDadosReceptora camada_Enlace_Dados_Receptora;

  // Construtor:
  public CamadaAplicacaoReceptora(AplicacaoReceptora aplicacao_Receptora) {
    this.aplicacao_Receptora = aplicacao_Receptora;
  }

  public void setCamadaEnlaceDadosReceptora (CamadaEnlaceDadosReceptora camada_Enlace_Dados_Receptora){
    this.camada_Enlace_Dados_Receptora = camada_Enlace_Dados_Receptora;
  }

  /* ***************************************************************
   * Metodo: camadaAplicacaoReceptora
   * Funcao: decodificar a mensagem, transformando cada 8 bits de
   * cada inteiro da mensagem em seu caracter da tabela AscII e
   * juntando-os numa unica mensagem
   * Parametros: int[] (contem a mensagem codificada em binario)
   * Retorno: vazio
   ****************************************************************/
  public void camadaAplicacaoReceptora(int quadro[]) {
    String[] mensagemDescodificada = new String[quadro.length * 4];
    String mensagem = "";
    try{
    // lendo o array de bits seprando-os de 8 em 8 (cada 8 eh um caracter)
    for (int i = 0; i < quadro.length; i++) { // repete o processo para cada int do array
      for (int j = 3; j >= 0; j--) { // repete 4 vezes para cada int (cada int guarda 4 caracteres)
        mensagemDescodificada[(i * 4) + (3 - j)] = "";
        for (int k = 7; k >= 0; k--) { // separa cada 8 bits numa unica String
          int mascara = 1 << (k + (j * 8));
          if ((mascara & quadro[i]) != 0) {
            mensagemDescodificada[(i * 4) + (3 - j)] += 1;
          } else {
            mensagemDescodificada[(i * 4) + (3 - j)] += 0;
          }
        } // fim do terceiro for
      } // fim do segundo for
    } // fim do primeiro for

    // exclui os bytes vazios
    for (int i = 1; i <= 4; i++) {
      if (mensagemDescodificada[mensagemDescodificada.length - i].equals("00000000")) {
        mensagemDescodificada[mensagemDescodificada.length - i] = null;
      }
    }
    // descodifica e junta todos os caracteres decodificados numa unica String
    for (String s : mensagemDescodificada) {
      if (s != null) {
        mensagem += Conversor.desconverteAscII(s);
      }
    }
    }catch(Exception a){
      System.out.println("Erro intransponivel 2");
      aplicacao_Receptora.aplicacaoReceptora("Mensagem nao enviada! Erro nos bist de controle");
    }
    System.out.println("\nCamada de Aplicacao Receptora:\n"+mensagem);
    // chama proxima camada
    aplicacao_Receptora.aplicacaoReceptora(mensagem);
  }// fim do metodo CamadaDeAplicacaoReceptora

  //chama o metodo parar da camada de enlace
  public void parar() {
    camada_Enlace_Dados_Receptora.parar();
  }

  public void avisaErro(){
    aplicacao_Receptora.setErro(true);
  }

}// fim da classe CamadaAplicacaoReceptora