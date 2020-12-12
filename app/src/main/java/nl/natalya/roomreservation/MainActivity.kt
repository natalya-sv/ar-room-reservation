package nl.natalya.roomreservation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.*
import nl.natalya.roomreservation.databinding.ActivityMainBinding
import nl.natalya.roomreservation.factory.APIFactory
import nl.natalya.roomreservation.factory.CalendarAPIFactory
import nl.natalya.roomreservation.helpers.CameraPermissionHelper
import nl.natalya.roomreservation.repositories.UserRepository
import nl.natalya.roomreservation.ui.SignInDialogFragment
import nl.natalya.roomreservation.viewmodels.UserViewModel
import nl.natalya.roomreservation.viewmodels.UserViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
     var session: Session? = null
    private lateinit var userModel: UserViewModel
    private var mUserRequestedInstall = true
    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val dataSource = UserRepository.getInstance()
        val viewModelFactory = UserViewModelFactory(dataSource)
        userModel = ViewModelProvider(this, viewModelFactory).get(UserViewModel::class.java)
        setSupportActionBar(mainBinding.myToolbar)
        mainBinding.bottomNavigation.visibility = View.GONE

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
            val signIn = SignInDialogFragment()
            val manager = supportFragmentManager
            val transaction = manager.beginTransaction()
            signIn.show(transaction, "sign_in")
        }

    }

    fun startApplication() {
        loadImages()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupBottomNavigationBar()
    }

    //    /**
    //     * Called on first creation and when restoring state.
    //     */
    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navGraphIds = listOf(R.navigation.home, R.navigation.room, R.navigation.location, R.navigation.profile, R.navigation.sign_in_nav)
        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(navGraphIds = navGraphIds, fragmentManager = supportFragmentManager,
                                                                     containerId = R.id.fragment_container, intent = intent)
        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, Observer { navController ->
            setupActionBarWithNavController(navController)
        })
        currentNavController = controller

        bottomNavigationView.disableTooltip()
    }

    //removes the tooltip when the item is clicked
    private fun BottomNavigationView.disableTooltip() {
        val content: View = getChildAt(0)
        if (content is ViewGroup) {
            content.forEach {
                it.setOnLongClickListener {
                    return@setOnLongClickListener true
                }
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    override fun onResume() {
        super.onResume()

        if (session == null) {
            var sException: Exception? = null
            var message: String? = null
            try {
                if (!CameraPermissionHelper().hasCameraPermission(this)) {
                    CameraPermissionHelper().requestCameraPermission(this);
                    return
                }
                when (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    ArCoreApk.InstallStatus.INSTALLED -> session = Session(this)
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> mUserRequestedInstall = false
                }

            } catch (exception: UnavailableUserDeclinedInstallationException) {
                message = this.resources.getString(R.string.install_arcore)
                sException = exception
            } catch (exception: UnavailableApkTooOldException) {
                message = this.resources.getString(R.string.update_arcore)
                sException = exception
            } catch (exception: UnavailableSdkTooOldException) {
                message = this.resources.getString(R.string.update_app)
                sException = exception
            } catch (exception: UnavailableDeviceNotCompatibleException) {
                message = this.resources.getString(R.string.ar_not_supported)
                sException = exception
            } catch (exception: UnavailableArcoreNotInstalledException) {
                message = this.resources.getString(R.string.install_arcore)
                sException = exception
            } catch (exception: Exception) {
                message = this.resources.getString(R.string.ar_session_failed)
                sException = exception
            }

            if (message != null) {
                showToastMessage(message + sException)
            }
        }
        Log.i("MainActivitySession", "Session is resumed")
    }

    override fun onPause() {
        super.onPause()
        session?.pause()
        Log.i("MainActivitySession", "Session is paused")
    }

    override fun onDestroy() {
        super.onDestroy()
        session?.close()
        session = null
        Log.i("MainActivitySession", "Session is destroyed")
    }

    /** Does the sign in, adds images to the database to be tracked */
    private fun loadImages() {
        session?.let { session ->
            CalendarAPIFactory().getCalendarAPI(APIFactory.ASP).addImagesToDatabase(session).thenApply { imageDatabaseResult ->
                if (imageDatabaseResult) {
                    showToastMessage(this.resources.getString(R.string.database_loaded_success))
                    session.close()
                }
                else {
                    showToastMessage(this.resources.getString(R.string.database_loaded_failure))
                }
            }.exceptionally {
                showToastMessage(this.resources.getString(R.string.interactive_sign_in_failure))
            }
        }
    }

    fun showUI() {
        mainBinding.bottomNavigation.visibility = View.VISIBLE
    //    supportActionBar?.show()
        hideProgressBar()
    }

    private fun hideProgressBar() {
        runOnUiThread {
            mainBinding.fragmentContainer.visibility = View.VISIBLE
            mainBinding.progressbar.visibility = View.GONE
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (!CameraPermissionHelper().hasCameraPermission(this)) {
            (this.resources.getString(R.string.camera_permission_message))

            if (!CameraPermissionHelper().shouldShowRequestPermissionRationale(this)) {
                this.let { CameraPermissionHelper().launchPermissionSettings(it) }
            }
        }
    }

    private fun showToastMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}
