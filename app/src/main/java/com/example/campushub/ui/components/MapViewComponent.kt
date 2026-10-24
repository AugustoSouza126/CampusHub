package com.example.campushub.ui.components

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.campushub.ui.theme.AppColors
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

/**
 * Componente reutilizável para exibição de mapas interativos utilizando o MapLibre SDK.
 * Evita duplicação de código de inicialização, controle de ciclo de vida e configuração de estilo.
 */
object MapViewComponent {

    /**
     * URL do estilo de mapa vetorial (padrão demo MapLibre open-source).
     */
    private const val MAP_STYLE_URL = "https://demotiles.maplibre.org/style.json"

    /**
     * Cria uma [View] contendo o mapa do MapLibre centralizado nas coordenadas informadas ([latitude], [longitude])
     * e adiciona um marcador com [titulo] e [subtitulo] do evento.
     */
    fun criarMapaLocalizacao(
        context: Context,
        latitude: Double,
        longitude: Double,
        titulo: String,
        subtitulo: String = "",
        alturaDp: Int = 220,
        zoom: Double = 15.0
    ): View {
        try {
            MapLibre.getInstance(context)
        } catch (_: Exception) {}

        val container = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                UIComponents.dpToPx(context, alturaDp)
            ).apply {
                setMargins(0, UIComponents.dpToPx(context, 8), 0, UIComponents.dpToPx(context, 8))
            }
            background = UIComponents.criarShape(context, AppColors.COLOR_CARD_BG, AppColors.COLOR_STROKE, 1, 12f)
            clipToOutline = true
        }

        val mapView = MapView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        container.addView(mapView)

        mapView.onCreate(null)

        val lifecycleOwner = context as? LifecycleOwner
        if (lifecycleOwner != null) {
            val observer = object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) { mapView.onStart() }
                override fun onResume(owner: LifecycleOwner) { mapView.onResume() }
                override fun onPause(owner: LifecycleOwner) { mapView.onPause() }
                override fun onStop(owner: LifecycleOwner) { mapView.onStop() }
                override fun onDestroy(owner: LifecycleOwner) {
                    mapView.onDestroy()
                    owner.lifecycle.removeObserver(this)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
        }

        mapView.getMapAsync { maplibreMap ->
            maplibreMap.setStyle(Style.Builder().fromUri(MAP_STYLE_URL)) { _ ->
                val pontoLatLng = LatLng(latitude, longitude)
                maplibreMap.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(pontoLatLng)
                            .zoom(zoom)
                            .build()
                    )
                )

                maplibreMap.addMarker(
                    MarkerOptions()
                        .position(pontoLatLng)
                        .title(titulo)
                        .snippet(subtitulo)
                )
            }
        }

        return container
    }
}
