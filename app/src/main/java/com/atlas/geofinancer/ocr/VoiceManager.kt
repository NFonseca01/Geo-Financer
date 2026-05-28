package com.atlas.geofinancer.ocr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * Gestor de Reconocimiento de Voz nativo y offline para accesibilidad.
 */
class VoiceManager(private val context: Context) {

    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

    /**
     * Inicia la escucha del micrófono.
     * @param onResultado Devuelve el texto dictado por el usuario.
     * @param onError Devuelve un mensaje de error legible si algo falla.
     */
    fun escucharVoz(onResultado: (String) -> Unit, onError: (String) -> Unit) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // Configura el modelo de lenguaje libre (búsqueda/dictado rápido)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            // Forzamos el idioma local del dispositivo (Español por defecto)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            // Intentar procesar de manera local/offline si el dispositivo lo soporta
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                val mensajeError = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Error de audio al grabar."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Faltan permisos de micrófono."
                    SpeechRecognizer.ERROR_NO_MATCH -> "No se entendió el audio, intenta de nuevo."
                    else -> "El dictado no está disponible en este momento."
                }
                onError(mensajeError)
            }

            override fun onResults(results: Bundle?) {
                // Recuperamos la lista de textos posibles que el motor cree haber escuchado
                val coincidencias = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!coincidencias.isNullOrEmpty()) {
                    // Tomamos la opción con mayor nivel de confianza (la primera)
                    onResultado(coincidencias[0])
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        // Arranca el micrófono del sistema
        speechRecognizer.startListening(intent)
    }

    /**
     * Detiene la escucha de forma manual si es necesario.
     */
    fun detenerEscucha() {
        speechRecognizer.stopListening()
    }

    /**
     * Un extractor inteligente de lenguaje natural adaptado para adultos mayores.
     * Si el usuario dice: "Gasté veinticinco pesos en la panadería" o "Cincuenta con diez en farmacia"
     * intentamos extraer el número y el comercio.
     */
    fun parsearDictadoVoz(textoDictado: String): Pair<String, Double> {
        val textoLower = textoDictado.lowercase()
        
        // 1. Intentar buscar números decimales o enteros en el texto
        val regexNumerica = "([0-9]+([.,][0-9]{1,2})?)".toRegex()
        val match = regexNumerica.find(textoLower)
        val monto = match?.value?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

        // 2. Intentar deducir el comercio limpiando conectores comunes
        var comercio = textoDictado
            .replace(regexNumerica, "") // Quitamos el precio del texto
            .replace("gasté", "", ignoreCase = true)
            .replace("pesos", "", ignoreCase = true)
            .replace("dólares", "", ignoreCase = true)
            .replace("en la", "", ignoreCase = true)
            .replace("en el", "", ignoreCase = true)
            .replace("en", "", ignoreCase = true)
            .trim()

        if (comercio.isBlank()) comercio = "Gasto por voz"

        return Pair(comercio, monto)
    }
}
