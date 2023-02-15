package com.example.test.ui.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.example.test.R
import com.example.test.utils.Variables
import com.example.test.databinding.ActivityPrincipalBinding
import com.example.test.ui.fragments.FragmentArgentina
import com.example.test.ui.fragments.FragmentFrancia
import com.google.android.material.snackbar.Snackbar
import kotlin.random.Random


class PrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrincipalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initActivity()
        initClicks()

        binding.apply { registerForContextMenu(binding.txtTitle) }


    }

    // Menu contextual
    override fun onCreateContextMenu(
        menu: ContextMenu, v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.contextual_menu, menu)
    }

    // Menu contextual
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.map_1 -> {
                openGoogleMaps()
                true
            }
            R.id.search_1 -> {
                googleSearch()
                true
            }
            R.id.share_1 -> {
                shareText()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }


    @Suppress("MissingPermission")
    private fun notification(
        CHANNEL_ID: String,
        CHANNEL_NAME: String,
        CHANNEL_DESC: String,
        title: String
    ) {

        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, PrincipalActivity::class.java).apply {
            putExtra("saludo","FINALIZAMOS")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)



        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_active_24)
            .setContentTitle(title)
            .setContentText("Contenido de la notificacion")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Este es un contenido mas largo para que la aplicacion funcione")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_NAME
            val descriptionText = CHANNEL_DESC
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            with(NotificationManagerCompat.from(this)) {
                // notificationId is a unique int for each notification that you must define
                val notificationId = Random.nextInt(0, 10000000)
                notify(notificationId, builder.build())
            }

        }

    }

    private fun initClicks() {
        binding.btnQuery.setOnClickListener {
            notification(
                "chat",
                "chat",
                "Este es el canal para envio de chats",
                "Mensaje en chats"
            )
            notification(
                "menciones",
                "menciones",
                "El grupo cuando te menciona en un chat",
                "Mensajes mensiones"
            )
        }

        binding.btnMap.setOnClickListener { openGoogleMaps() }
        binding.btnShare.setOnClickListener { shareText() }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.idFrancia -> {
                    fragmentVisibility(FragmentFrancia())
                    true
                }

                R.id.idArgentina -> {
                    fragmentVisibility(FragmentArgentina())
                    true
                }
                else -> false
            }
        }

    }

    // Manejo de fragmentos
    private fun fragmentVisibility(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.FragmentPrincipal.id, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    // Menu de compartir
    private fun shareText() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, binding.txtQuery.text.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Chequea si el paquete esta instalado en el dispositivo
    private fun checkPackage(namePackage: String): Boolean {

        try {
            this.packageManager.getApplicationInfo(
                namePackage,
                PackageManager.GET_META_DATA
            )

        } catch (e: PackageManager.NameNotFoundException) {

        }
        return true
    }


    // Abre la play store para la instalacion de la aplicacion solicitada
    private fun openPlayStore(namePackage: String) {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$namePackage")
                )
            )
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$namePackage")
                )
            )
        }
    }

    // Abre google maps
    private fun openGoogleMaps() {
        val namePackage = "com.google.android.apps.maps"
        if (checkPackage(namePackage)) {
            // Create a Uri from an intent string. Use the result to create an Intent.
            // Street view
            //val gmmIntentUri = Uri.parse("google.streetview:cbll=-0.2032731,-78.5008713")
            val location = Uri.parse("geo:0,0?q=" + binding.txtQuery.text.toString())

            // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
            var mapIntent = Intent(Intent.ACTION_VIEW, location)

            // Make the Intent explicit by setting the Google Maps package
            mapIntent.setPackage(namePackage)

            // Attempt to start an activity that can handle the Intent
            try {
                startActivity(mapIntent)
            } catch (e: ActivityNotFoundException) {
                Snackbar.make(
                    binding.txtTitle,
                    "Aplicación no encontrada",
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            }
        } else {
            openPlayStore(namePackage)
        }
    }

    // Abre la busqueda de google
    private fun googleSearch() {
        val namePackage = "com.google.android.googlequicksearchbox"
        if (checkPackage(namePackage)) {
            val intent = Intent(Intent.ACTION_WEB_SEARCH)
            intent.setClassName(
                namePackage,
                "com.google.android.googlequicksearchbox.SearchActivity"
            )
            intent.putExtra("query", binding.txtQuery.text.toString());
            startActivity(intent)
        } else {
            openPlayStore(namePackage)
        }
    }

    private fun initActivity() {
        intent.extras?.let {
            val saludo = it.getString(
                Variables.nombreUsuario,
                "No hay dato"
            ).toString()
            binding.btnQuery.text = saludo
        }

        val saludo = intent.extras?.getString(
            Variables.nombreUsuario,
            "No hay dato"
        ).toString()

    }

    override fun onResume() {
        super.onResume()
        initActivity()
    }

}
