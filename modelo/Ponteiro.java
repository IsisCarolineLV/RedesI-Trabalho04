/* ***************************************************************
* Autor............: Isis Caroline Lima Viana
* Matricula........: 202410016
* Inicio...........: 15/10/2025
* Ultima alteracao.: 02/11/2025
* Nome.............: Ponteiro.java
* Funcao...........: Classe auxiliar para armazenar as coordenadas
* (indice do array e posicao no inteiro) de inicio e fim de um
* quadro de bits dentro de um array de inteiros.
*************************************************************** */
package modelo;
public class Ponteiro {
  private int posicaoNoInteiroInicio;
  private int posicaoNoArrayInicio;
  private int posicaoNoInteiroFim;
  private int posicaoNoArrayFim;
  private int id; //so para eu me guiar nas impressoes no console
  protected static int cont; 

  //Construtor
  public Ponteiro(int posicaoNoInteiroInicio,int posicaoNoArrayInicio) {
    this.posicaoNoArrayInicio = posicaoNoArrayInicio;
    setIntInicio(posicaoNoInteiroInicio);
    id=cont++;
  }

  /* ***************************************************************
   * Metodo: setFimQuadro
   * Funcao: Define as coordenadas de fim do quadro.
   * Parametros: int posicaoNoInteiroFim, int posicaoNoArrayFim
   * Retorno: void
   ****************************************************************/
  public void setFimQuadro(int posicaoNoInteiroFim,int posicaoNoArrayFim) {
    this.posicaoNoArrayFim = posicaoNoArrayFim;
    setIntFim(posicaoNoInteiroFim);
  }

  /* ***************************************************************
   * Metodo: setInicioQuadro
   * Funcao: Define as coordenadas de inicio do quadro.
   * Parametros: int posicaoNoInteiroInicio, int posicaoNoArrayInicio
   * Retorno: void
   ****************************************************************/
  public void setInicioQuadro(int posicaoNoInteiroInicio,int posicaoNoArrayInicio) {
    this.posicaoNoArrayInicio = posicaoNoArrayInicio;
    setIntInicio(posicaoNoInteiroInicio);
  }

  //GETS:
  public int intInicio() {
    return posicaoNoInteiroInicio;
  }

  public int intFim() {
    return posicaoNoInteiroFim;
  }

  public int arrayInicio() {
    return posicaoNoArrayInicio;
  }

  public int arrayFim() {
    return posicaoNoArrayFim;
  }

  //SETS:
  public void setIntInicio(int intInicio){
    if(intInicio<0){ 
      posicaoNoInteiroInicio = 32+intInicio;
      posicaoNoArrayInicio++;
    }
    else
      posicaoNoInteiroInicio = intInicio;
  }

  public void setIntFim(int intFim){
    if(intFim<0){
      posicaoNoInteiroFim = 32+intFim;
      posicaoNoArrayFim++;
    }
    else
      posicaoNoInteiroFim = intFim;
  }

  /* ***************************************************************
   * Metodo: tamanhoQuadro
   * Funcao: Calcula o numero total de bits entre o ponteiro de
   * inicio e o ponteiro de fim.
   * Parametros: void
   * Retorno: int (total de bits)
   ****************************************************************/
  public int tamanhoQuadro(){
    if(intInicio()==intFim()) 
      return (arrayFim() - arrayInicio()) * 32;
    else if(intInicio()>intFim())
      return (arrayFim() - arrayInicio()) * 32 + (intInicio()-intFim())+1;
    else
      return (arrayFim() - arrayInicio()) * 32  - (intFim()-intInicio())+1;
  }

  /* ***************************************************************
   * Metodo: add
   * Funcao: Avanca o ponteiro de fim em posicoes,
   * ajustando a posicao no inteiro e o indice do array.
   * Parametros: int bits (numero de bits a avancar)
   * Retorno: void
   ****************************************************************/
  public void add(int bits){
    //System.out.println("Adicionou "+bits);
    posicaoNoArrayFim += (bits/32);
    posicaoNoInteiroFim -= (bits%32);
    if(posicaoNoInteiroFim<0){
      posicaoNoArrayFim++;
      posicaoNoInteiroFim = 32 + posicaoNoInteiroFim;
    }
  }

  /* ***************************************************************
   * Metodo: imp
   * Funcao: Metodo para imprimir as coordenadas de inicio e fim do 
   * ponteiro no console.
   * Parametros: void
   * Retorno: void
   ****************************************************************/
  public void imp(){
    System.out.println("Ponteiro:"+id+"\n"
                          +"("+posicaoNoInteiroInicio+","+posicaoNoArrayInicio+")\n"
                          +"("+posicaoNoInteiroFim+","+posicaoNoArrayFim+")");
  }
}
