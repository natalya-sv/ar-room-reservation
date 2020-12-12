package nl.natalya.roomreservation.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.busy_room_view.view.*
import kotlinx.android.synthetic.main.free_room_view.view.*
import nl.natalya.roomreservation.R
import nl.natalya.roomreservation.ServiceLocator
import nl.natalya.roomreservation.adapters.MeetingAdapter
import nl.natalya.roomreservation.data.*
import nl.natalya.roomreservation.databinding.FragmentRoomFinderBinding
import nl.natalya.roomreservation.factory.APIFactory
import nl.natalya.roomreservation.helpers.CameraPermissionHelper
import nl.natalya.roomreservation.repositories.MeetingRoomRepository
import nl.natalya.roomreservation.ui.meetingRoom.MeetingRoomViewModel
import nl.natalya.roomreservation.ui.meetingRoom.MeetingRoomViewModelFactory
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.sqrt

class RoomFinderFragment : Fragment() {

    private lateinit var roomFinderBinding: FragmentRoomFinderBinding
    private lateinit var arFragment: ArFragment
    private var shouldConfigureSession = false
    private var scannedImages = arrayListOf<String>()
    private var session: Session? =null
    private var dateFormat = DateTimeFormatter.ofPattern("dd LLLL yyyy")
    private lateinit var meetingRoomViewModel: MeetingRoomViewModel
    private val currentAPI = APIFactory.ASP
    private lateinit var anchor: Anchor

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        roomFinderBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_room_finder, container, false)

        val dataSource = MeetingRoomRepository.getInstance()
        val viewModelFactory = MeetingRoomViewModelFactory(dataSource)
        meetingRoomViewModel = ViewModelProvider(this, viewModelFactory).get(MeetingRoomViewModel::class.java)

        return roomFinderBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        arFragment = childFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment
        arFragment.arSceneView.scene.addOnUpdateListener { onUpdateFrame() }
        arFragment.planeDiscoveryController.hide()
        arFragment.planeDiscoveryController.setInstructionView(null)
        arFragment.arSceneView.planeRenderer.isEnabled = false

        meetingRoomViewModel.meetingRoomInformation.observe(viewLifecycleOwner, Observer { wrapper ->
            if (wrapper != null) {
                checkRequestResult(wrapper)
            }
        })
    }

    private fun configureSession() {
        val config = Config(session)
        config.focusMode = Config.FocusMode.AUTO
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        config.augmentedImageDatabase = ServiceLocator.getAugmentedImagesDatabase().imagesDatabase
        session?.configure(config)

        val filter = CameraConfigFilter(session!!)
        filter.depthSensorUsage = EnumSet.of(CameraConfig.DepthSensorUsage.DO_NOT_USE)
        filter.targetFps = EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_30)
    }

    /** Meeting room images tracking */
    private fun onUpdateFrame() {
        val frame = session?.update()
        val augmentedImages = frame!!.getUpdatedTrackables(AugmentedImage::class.java)
        val pose = Pose.makeRotation(-sqrt(0.5).toFloat(), 0.0f, 0.0f, sqrt(0.5).toFloat())

        /** Compares the current image in the video stream with images from the database */
        for (augmentedImage in augmentedImages) {
            if (augmentedImage.trackingState == TrackingState.TRACKING) {

                /** If the image is recognized it is added to the array, so not to call the API many times */
                if (!scannedImages.contains(augmentedImage.name)) {
                    scannedImages.add(augmentedImage.name)
                    anchor = augmentedImage.createAnchor(augmentedImage.centerPose.compose(pose))
                    meetingRoomViewModel.getRoomAvailabilityInformation(augmentedImage.name, currentAPI)
                }
            }
        }
    }

    private fun checkRequestResult(meetingRoomWrapper: MeetingRoomWrapper) {
        if (meetingRoomWrapper.httpRequestResultSuccess) {
            meetingRoomWrapper.meetingRoom?.let { prepareResultsToDisplay(anchor, it) }
        }
        else {
            showToastMessage(meetingRoomWrapper.exception?.cause?.message.toString())
        }
    }

    /** When room availability information is received, it prepares it to display depending on busy or free status */
    private fun prepareResultsToDisplay(anchor: Anchor, meetingRoom: MeetingRoom) {
        val numberOfBookings = meetingRoom.reservations.size
        val roomIsFreeNow: Boolean
        val busyRoomView = R.layout.busy_room_view
        val freeRoomView = R.layout.free_room_view
        val requiredView: Int
        lateinit var firstBookingInTheList: MeetingRoomReservation
        val currentTime = LocalTime.now()

        if (numberOfBookings > 0) {
            meetingRoom.reservations.sortBy { it.startTime }
            firstBookingInTheList = meetingRoom.reservations[0]

            if (currentTime < firstBookingInTheList.startTime && currentTime < firstBookingInTheList.endTime) {
                requiredView = freeRoomView
                roomIsFreeNow = true
                meetingRoom.currentStatus = STATUS.FREE
            }
            else if (currentTime > firstBookingInTheList.startTime && currentTime > firstBookingInTheList.endTime) {
                requiredView = freeRoomView
                roomIsFreeNow = true
                meetingRoom.currentStatus = STATUS.FREE
            }
            else {
                requiredView = busyRoomView
                roomIsFreeNow = false
                meetingRoom.currentStatus = STATUS.BUSY
            }
        }
        else {
            requiredView = freeRoomView
            roomIsFreeNow = true
            meetingRoom.currentStatus = STATUS.FREE
        }
        activity?.runOnUiThread {
            createRenderableView(anchor, requiredView, meetingRoom, roomIsFreeNow)
        }
    }

    /** Creates AR view */
    private fun createRenderableView(anchor: Anchor, requiredView: Int, meetingRoom: MeetingRoom, roomIsFreeNow: Boolean) {
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment.arSceneView.scene)
        val node = Node()
        node.setParent(anchorNode)
        lateinit var createdView: View

        ViewRenderable.builder().setView(context, requiredView).build().thenAccept { renderable: ViewRenderable ->
            renderable.isShadowCaster = false
            renderable.isShadowReceiver = false
            node.renderable = renderable
            createdView = renderable.view

            if (roomIsFreeNow) {
                displayFreeRoom(anchor, createdView, meetingRoom, anchorNode, node)
            }
            else {
                displayBusyRoom(createdView, meetingRoom)
            }
        }.exceptionally {
            activity?.runOnUiThread {
                Toast.makeText(context, resources.getString(R.string.load_ar_failure), Toast.LENGTH_SHORT).show()
            }
            null
        }
    }

    /** Displays free room information and the user can book that room */
    private fun displayFreeRoom(anchor: Anchor, createdView: View, meetingRoom: MeetingRoom, anchorNode: AnchorNode, node: Node) {
        val numberOfBookings = meetingRoom.reservations.size
        var freeStatus = context?.resources?.getString(R.string.free_word)

        if (numberOfBookings > 0) {
            createdView.next_reservation_view_free.visibility = View.GONE
            freeStatus = context?.resources?.getString(R.string.free_until) + meetingRoom.reservations[0].startTime

            if(LocalTime.now().toString() > meetingRoom.reservations[0].startTime.minusMinutes(15).toString()){
                activity?.runOnUiThread {
                    createdView.room_reservation_button.isEnabled = false
                    createdView.room_reservation_button.setBackgroundResource(R.drawable.gray_round_button)
                }
            }

        }
        val meetingsListAdapter = MeetingAdapter(requireContext(), meetingRoom.reservations)

        createdView.free_room_name.text = meetingRoom.meetingRoomName
        createdView.room_availability_text.text = freeStatus
        createdView.meetings_List_free.adapter = meetingsListAdapter

        /** User clicks the button to book a room */
        createdView.room_reservation_button.setOnClickListener {
            view?.findNavController()?.navigate(RoomFinderFragmentDirections.actionRoomFinderFragmentToRoomReservationFragment(meetingRoom))
        }
    }

    private fun displayBusyRoom(createdView: View, meetingRoom: MeetingRoom) {

        val numberOfBookings = meetingRoom.reservations.size
        if (numberOfBookings > 1) {
            createdView.next_reservation_view_busy.visibility = View.GONE
        }

        val meetingsListAdapter = MeetingAdapter(requireContext(), meetingRoom.reservations)
        createdView.busy_room_name.text = meetingRoom.meetingRoomName
        createdView.room_availability.text = meetingRoom.currentStatus.toString()
        createdView.date.text = meetingRoom.reservations[0].meetingDate.format(dateFormat).toString()
        createdView.startTimeReservation.text = meetingRoom.reservations[0].startTime.toString()
        createdView.endTimeReservation.text = meetingRoom.reservations[0].endTime.toString()
        createdView.roomOrganizer.text = meetingRoom.reservations[0].user.name.plus(" ").plus(meetingRoom.reservations[0].user.surname)
        createdView.meetingTopicText.text = meetingRoom.reservations[0].meetingTopic
        meetingRoom.reservations.removeAt(0)
        createdView.meetingList.adapter = meetingsListAdapter
    }

    override fun onPause() {
        super.onPause()
        if (session != null) {
            arFragment.arSceneView?.pause()
            session?.pause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        session = null
        scannedImages.clear()
        meetingRoomViewModel.clearTheModel()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (!CameraPermissionHelper().hasCameraPermission(activity)) {
            showToastMessage(requireContext().resources.getString(R.string.camera_permission_message))

            if (!CameraPermissionHelper().shouldShowRequestPermissionRationale(activity)) {
                activity?.let { CameraPermissionHelper().launchPermissionSettings(it) }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (session == null) {
            var sException: Exception? = null
            var message: String? = null

            try {
                if (!CameraPermissionHelper().hasCameraPermission(activity)) {
                    CameraPermissionHelper().requestCameraPermission(activity);
                    return
                }

                session = Session(context)

            } catch (exception: UnavailableUserDeclinedInstallationException) {
                message = context?.resources?.getString(R.string.install_arcore)
                sException = exception
            } catch (exception: UnavailableApkTooOldException) {
                message = context?.resources?.getString(R.string.update_arcore)
                sException = exception
            } catch (exception: UnavailableSdkTooOldException) {
                message = context?.resources?.getString(R.string.update_app)
                sException = exception
            } catch (exception: UnavailableDeviceNotCompatibleException) {
                message = context?.resources?.getString(R.string.ar_not_supported)
                sException = exception
            } catch (exception: Exception) {
                message = context?.resources?.getString(R.string.ar_session_failed)
                sException = exception
            }

            if (message != null) {
                showToastMessage(message + sException.toString())
            }
            shouldConfigureSession = true
        }

        if (shouldConfigureSession) {
            configureSession()
            shouldConfigureSession = false
            arFragment.arSceneView?.setupSession(session)
        }

        try {
            session?.resume()
        } catch (exception: CameraNotAvailableException) {
            showToastMessage(exception.toString())
        }

        arFragment.onResume()
    }

    private fun showToastMessage(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        fun newInstance(): RoomFinderFragment {
            return RoomFinderFragment()
        }
    }
}

