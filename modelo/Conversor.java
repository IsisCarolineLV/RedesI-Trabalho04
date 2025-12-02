/* ***************************************************************
* Autor............: Isis Caroline Lima Viana
* Matricula........: 202410016
* Inicio...........: 16/08/2025
* Ultima alteracao.: 29/09/2025
* Nome.............: Conversor.java
* Funcao...........: Essa interfaceeh utlizada para realizar as 
conversoes de ascii binario para char e vice-versa. Classe auxiliar.
*************************************************************** */
package modelo;

public interface Conversor{
  
  /* ***************************************************************
   * Metodo: converteAscII
   * Funcao: recebe um caracter e o converte para seu equivalente 
   * binario (em forma de String)
   * Parametros: char (caracter a ser convertido)
   * Retorno: String char em ascii
   ****************************************************************/
  public static String converteAscII(char caracter){
    String a = Integer.toBinaryString(caracter);
    while(a.length()<8)
      a = 0+a;
    return a;
  }
  
  /* ***************************************************************
   * Metodo: desconverteAscII
   * Funcao: recebe uma String de ascii binario e a converte para seu 
   * caracter equivalente 
   * Parametros: String (codigo ascii)
   * Retorno: char traduzida
   ****************************************************************/
  public static char desconverteAscII(String charEmBinario){
    char caracter = (char) Integer.parseInt(charEmBinario, 2);
    return caracter;
  }
  
} //fim da interface Conversor