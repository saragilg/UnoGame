package net.azarquiel.appjuegouno

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import net.azarquiel.appjuegouno.model.Carta

//para saber la que se ha pulsado : listener
class MainActivity : AppCompatActivity(), View.OnClickListener {
    val colores = arrayOf("amarillo", "rojo", "verde", "azul")
    //estan vacios pero las cartas luego son las mismas, haya mas o haya menos
    //lo creamos pero lo inicializamos luego
    lateinit var mazo : ArrayList<Carta>
    lateinit var jugador : ArrayList<Carta>
    lateinit var maquina : ArrayList<Carta>
    //carta vacia, luego va cambiando
    lateinit var cartaJuego : Carta

    var turno = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nuevaPartida()
        cambioJugador()
        taparCartas(cartasMaquina)

        //trazabilidad()
        //trazabilidadCartas()
    }

    //para reconocer nuestro codigo en el logcat
    companion object{
        const val TAG = "GameUNO"
    }

    private fun nuevaPartida() {
        mezcla()
        repartir()
        pintarImg()
        monton.setOnClickListener{OnClickMazo()}
    }

    private fun mezcla() {
        mazo = ArrayList()
        //array : rellena cada hueco con el mismo numero del indice
        val vector = IntArray(80){ i -> i}
        //lo mezcla
        vector.shuffle()
        //indices
        var cont = 0

        //metemos 80 cartas vacias al mazo
        for (i in 0 until 80){
            mazo.add(Carta())
        }

        for (rep in 1..2){
            for (color in 0 until 4) {
                for (num in 0 until 10) {
                    //modificamos directamente sobre la carta
                    mazo[vector[cont]].numero=num
                    mazo[vector[cont]].color=color
                    cont++
                }
            }
        }
    }

    private fun sacarCarta(): Carta {
        val carta = mazo[0]
        mazo.removeAt(0)
        return carta
    }

    private fun repartir() {
        jugador = ArrayList()
        maquina = ArrayList()
        for (i in 1..7){
            jugador.add(sacarCarta())
            maquina.add(sacarCarta())
        }
        cartaJuego = sacarCarta()
    }

    //necesita el linear donde van las cartas y las cartas de cada uno
    private fun imgCartas(linear:LinearLayout, cartas:ArrayList<Carta>){
        //crear la vista
        var iv:ImageView
        var id:Int
        //crear las 7 vistas que seran cartas
        for (i in 0 until 7){
            //cada una de las iv de cartas
            iv = ImageView(this)
            id = resources.getIdentifier("${colores[cartas[i].color]}${cartas[i].numero}", "drawable", packageName)
            //poner la imagen
            iv.setBackgroundResource(id)
            //añadirla al linear layout
            linear.addView(iv)
            //para reconocer cada carta despues en el juego : necesita un TAG
            iv.tag = cartas[i]
            //listener para saber que se ha pulsado
            iv.setOnClickListener(this)
        }
    }

    private fun pintarImg() {
        //carta central
        val id = resources.getIdentifier("${colores[cartaJuego.color]}${cartaJuego.numero}", "drawable", packageName)
        cartaMesa.setBackgroundResource(id)

        //el mismo metodo que dibuja las imagenes vale para maquina y jugador
        //cartas jugador
        imgCartas(cartasJugador, jugador)
        //cartas maquina
        imgCartas(cartasMaquina, maquina)
    }

    private fun disable(linear: LinearLayout) {
        var iv:ImageView
        for (i in 0 until linear.childCount) {
            iv = linear.getChildAt(i) as ImageView
            iv.isEnabled = false
        }
    }
    private fun enable(linear: LinearLayout) {
        var iv:ImageView
        for (i in 0 until linear.childCount) {
            iv = linear.getChildAt(i) as ImageView
            iv.isEnabled = true
        }
    }

    private fun destaparCartas(linear: LinearLayout) {
        var iv:ImageView
        for (i in 0 until linear.childCount) {
            iv = linear.getChildAt(i) as ImageView
            iv.setImageResource(android.R.color.transparent)
        }
    }

    private fun taparCartas(linear: LinearLayout) {
        var iv:ImageView
        //maquina: hasta las image view que tenga
        for (i in 0 until linear.childCount) {
            iv = linear.getChildAt(i) as ImageView
            iv.setImageResource(R.drawable.reverso)
        }
    }

    private fun cambioJugador() {
        turno = !turno
        if (turno) {
            disable(cartasMaquina)
            taparCartas(cartasMaquina)
            enable(cartasJugador)
            destaparCartas(cartasJugador)
        }
        else {
            disable(cartasJugador)
            taparCartas(cartasJugador)
            enable(cartasMaquina)
            destaparCartas(cartasMaquina)
        }

    }

    private fun OnClickMazo() {
        //si no tenemos una carta del mismo color o el mismo numero con otro color hay que robar
        val iv = ImageView(this)
        val id:Int

        val cartaNueva = sacarCarta()
        iv.tag = cartaNueva
        iv.setOnClickListener(this)
        id = resources.getIdentifier("${colores[cartaNueva.color]}${cartaNueva.numero}", "drawable", packageName)
        iv.setBackgroundResource(id)
        mazo.remove(cartaNueva)

        if (turno){
            cartasJugador.addView(iv)
            cambioJugador()
        }else{
            cartasMaquina.addView(iv)
            cambioJugador()
        }
    }

    //para saber que carta se ha pulsado
    override fun onClick(v: View?) {
        //image view pulsado
        val ivPulsado = v as ImageView
        //le ponemos a ese image view el tag de la carta
        val cartaSeleccionada = ivPulsado.tag as Carta

        //mensaje("Carta pulsada: ${colores[cartaSeleccionada.color]}${cartaSeleccionada.numero}")

        //cuando se pulse la carta, se añade al mazo, se pone en la carta de juego y  se elimina
        //comprobar si se puede echar esa carta

        if (turno){
            if (cartaSeleccionada.color  == cartaJuego.color || cartaSeleccionada.numero == cartaJuego.numero ){
                mazo.add(cartaSeleccionada)
                val id = resources.getIdentifier("${colores[cartaSeleccionada.color]}${cartaSeleccionada.numero}", "drawable", packageName)
                cartaMesa.setBackgroundResource(id)
                cartaJuego = cartaSeleccionada

                cartasJugador.removeView(ivPulsado)
                cambioJugador()
            }
        }else{
            if (cartaSeleccionada.color  == cartaJuego.color || cartaSeleccionada.numero == cartaJuego.numero ){
                mazo.add(cartaSeleccionada)
                val id = resources.getIdentifier("${colores[cartaSeleccionada.color]}${cartaSeleccionada.numero}", "drawable", packageName)
                cartaMesa.setBackgroundResource(id)
                cartaJuego = cartaSeleccionada

                cartasMaquina.removeView(ivPulsado)
                cambioJugador()
            }
        }

        if(cartasJugador.childCount==0 || cartasMaquina.childCount == 0){
            Toast.makeText(this, "La partida ha finalizado", Toast.LENGTH_LONG).show()
            nuevaPartida()
            Toast.makeText(this, "NUEVA PARTIDA", Toast.LENGTH_LONG).show()
        }
    }

    private fun trazabilidad(){
        mazo.forEach {
            Log.d(TAG, "${it.numero} - ${colores[it.color]}")
        }

        Log.d(TAG, "---------------------------------------------------------")
    }
    private fun trazabilidadCartas(){
        Log.d(TAG, "----Maquina")
        maquina.forEach {
            Log.d(TAG, "${it.numero} - ${colores[it.color]}")
        }
        Log.d(TAG, "----Jugador")
        jugador.forEach {
            Log.d(TAG, "${it.numero} - ${colores[it.color]}")
        }

        Log.d(TAG, "----CARTA JUEGO")
        Log.d(TAG, "${cartaJuego.numero} - ${colores[cartaJuego.color]}")

    }

}