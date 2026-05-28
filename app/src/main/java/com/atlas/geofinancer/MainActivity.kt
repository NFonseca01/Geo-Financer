package com.atlas.geofinancer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.atlas.geofinancer.ui.screens.MainScreen
import com.atlas.geofinancer.ui.screens.FinancialViewModel

/**
 * APRENDIZAJE - ¿Qué es la MainActivity?
 * Es el punto de entrada que el sistema operativo Android busca cuando el usuario pulsa el icono de la app.
 * Hereda de 'ComponentActivity', que nos da soporte nativo para Jetpack Compose y la gestión moderna de permisos.
 */
class MainActivity : ComponentActivity() {

    // APRENDIZAJE - Delegación de Ciclo de Vida ('by viewModels()')
    // Inicializamos nuestro intermediario de datos. Este delegado asegura que si el usuario mayor
    // cambia la orientación del teléfono, los textos o datos cargados no se destruyan ni se pierdan.
    private val financialViewModel: FinancialViewModel by viewModels()

    // Sistema moderno de peticiones de componentes del sistema (Permisos nativos)
    private val solicitarPermisosLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { esConcedido ->
        if (esConcedido) {
            Toast.makeText(this, "Permiso de micrófono concedido", Toast.LENGTH_SHORT).show()
        } else {
            // Explicación guiada: Si lo rechaza, se le informa la alternativa de forma clara y accesible
            Toast.makeText(
                this, 
                "El dictado por voz no estará activo. Podrás seguir registrando manualmente.", 
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificamos el estado del micrófono al arrancar
        verificarYPedirPermisos()

        // Pintamos el árbol de interfaz reactiva de Compose
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Llamamos a nuestra vista accesible, inyectándole su motor lógico
                    MainScreen(viewModel = financialViewModel)
                }
            }
        }
    }

    /**
     * Comprueba las directivas de seguridad de Android. Si el usuario no ha aprobado
     * el uso del hardware del micrófono, despliega el diálogo nativo automáticamente.
     */
    private fun verificarYPedirPermisos() {
        val estadoPermiso = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        if (estadoPermiso != PackageManager.PERMISSION_GRANTED) {
            solicitarPermisosLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}
