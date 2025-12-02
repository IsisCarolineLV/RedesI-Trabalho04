/* ***************************************************************
* Autor............: Isis Caroline Lima Viana
* Matricula........: 202410016
* Inicio...........: 16/08/2025
* Ultima alteracao.: 28/08/2025
* Nome.............: CamadaAplicacaoTransmissora.java
* Funcao...........: Essa camada eh responsavel por codificar a 
mensagem, transformando cada caracter da mensagem em seu equivalente
binario da tabela AscII e guardando-os de bit a bit em inteiros, e
por fim envia o array de inteiros para a camada fisica tarnsmissora
*************************************************************** */
package modelo;

public class CamadaAplicacaoTransmissora{
  
  private CamadaEnlaceDadosTransmissora camada_Enlace_Dados_Transmissora;
  
  //Construtor:
  public CamadaAplicacaoTransmissora(CamadaEnlaceDadosTransmissora camada_Enlace_Dados_Transmissora){
    this.camada_Enlace_Dados_Transmissora = camada_Enlace_Dados_Transmissora;
  }
  
  /* ***************************************************************
   * Metodo: CamadaAplicacaoTransmissora
   * Funcao: Codificar a mensagem em AscII e aramzena-la bit a bit
   * num array de inteiros
   * Parametros: mensagem
   * Retorno: vazio
   * ****************************************************************/
  public void camadaAplicacaoTransmissora (String mensagem) {
    char[] caracteres = mensagem.toCharArray();
    String[] mensagemEmBinario = new String[caracteres.length];
    
    //transforma cada caracter em uma String com seu equivalente em AscII
    for(int i=0; i<caracteres.length; i++){
      mensagemEmBinario[i] = Conversor.converteAscII(caracteres[i]);
    }
    
    int quantosInt = (int) (mensagemEmBinario.length + 3)/4; //formula para arredondar pra cima
    int[] fluxoBrutoDeBits = new int[quantosInt];
    int contLeitor = 0;
    for(int f=0; f<quantosInt; f++){  //repete o processo para cada int necessario
      int primeiraPosicao = 31;
      for(int i=0; i<4; i++){ //guarda 4 bytes em cada int
        if(contLeitor>=mensagemEmBinario.length) break;
        String letraCodificada = mensagemEmBinario[contLeitor++];
        for(int j=0; j<8; j++){ //guarda os 8 bits no inteiro
          if(letraCodificada.charAt(j)=='1'){
            fluxoBrutoDeBits[f] = fluxoBrutoDeBits[f] | 1<<primeiraPosicao;
          } 
          primeiraPosicao--; 
        } //fim do terceiro for
      } //fim do segundo for
    } //fim do primeiro for
    System.out.println("\nCamada de Aplicacao Transmissora:");
    imprimir(fluxoBrutoDeBits);
    //chama a proxima camada
    camada_Enlace_Dados_Transmissora.camadaEnlaceDadosTransmissora(fluxoBrutoDeBits);
  }//fim do metodo CamadaDeAplicacaoTransmissora
  
  /* ***************************************************************
   * Metodo: parar
   * Funcao: faz a camada fisica parar
   * Parametros: nenhum
   * Retorno: vazio
   * ****************************************************************/
  public void parar(){
    camada_Enlace_Dados_Transmissora.parar();
  }

  public void setTaxaDeErro(int taxa){
    camada_Enlace_Dados_Transmissora.setTaxaDeErro(taxa);
  }
  
  /* ****************** METODOS DE IMPRESSAO ****************** */
  public void imprimir(int[] vetor) {
    for (int a : vetor) {
      String bits32 = String.format("%32s", Integer.toBinaryString(a)).replace(' ', '0');
      System.out.println(bits32);
    }
  }
  
} //fim da classe CamadaAplicacaoTransmissora