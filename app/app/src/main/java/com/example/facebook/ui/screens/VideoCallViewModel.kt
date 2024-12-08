package com.example.facebook.ui.screens

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.facebook.FacebookApplication
import com.example.facebook.data.SocketRepository
import com.example.facebook.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

data class UserUiState(
    val socketId: String,
    val id: String,
    var peerConnection: PeerConnection,
    val surfaceViewRenderer: SurfaceViewRenderer,
)

data class VideoCallUiState(
    val users: List<UserUiState> = emptyList(),
    val mainSurfaceViewRenderer: SurfaceViewRenderer,
)

class VideoCallViewModel(
    private val application: FacebookApplication,
    private val userRepository: UserRepository,
    private val socketRepository: SocketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        VideoCallUiState(
            mainSurfaceViewRenderer = SurfaceViewRenderer(application)
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        initPeerConnectionFactory()
    }

    fun initVideoCall(id: String) {
        socketRepository.sendMessage("join", "call", id)
        socketRepository.sendMessage("call", id, JSONObject().apply {
            put("type", "join")
            put("from", socketRepository.getID())
            put("id", application.user._id)
        })
        socketRepository.addEventListener("call") { it ->
            val data = it[0] as JSONObject
            val type = data.getString("type")
            Log.d("RTCClient", "Received message: $data")
            when (type) {

                "join" -> {
                    val surfaceViewRenderer = SurfaceViewRenderer(application)
                    initializeSurfaceView(surfaceViewRenderer)

                    val peerConnection = createPeerConnection(object : PeerConnection.Observer {
                        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
                            Log.d("RTCClient", "onSignalingChange: $p0")
                        }

                        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                            Log.d("RTCClient", "onIceConnectionChange: $p0")
                        }

                        override fun onIceConnectionReceivingChange(p0: Boolean) {
                            Log.d("RTCClient", "onIceConnectionReceivingChange: $p0")
                        }

                        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                            Log.d("RTCClient", "onIceGatheringChange: $p0")
                        }

                        override fun onIceCandidate(p0: IceCandidate?) {
                            Log.d("RTCClient", "onIceCandidate: $p0")
                            socketRepository.sendMessage("call", id, JSONObject().apply {
                                put("type", "candidate")
                                put("from", socketRepository.getID())
                                put("to", data.getString("from"))
                                put("candidate", JSONObject().apply {
                                    put("candidate", p0?.sdp)
                                    put("sdpMid", p0?.sdpMid)
                                    put("sdpMLineIndex", p0?.sdpMLineIndex)
                                })
                            })
                        }

                        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
                            Log.d("RTCClient", "onIceCandidatesRemoved: $p0")
                        }

                        override fun onAddStream(mediaStream: MediaStream?) {
                            Log.d("RTCClient", "onAddStream: $mediaStream")
                            mediaStream?.videoTracks?.get(0)?.addSink(surfaceViewRenderer)
                            mediaStream?.audioTracks?.get(0)?.setEnabled(true)
                        }

                        override fun onTrack(transceiver: RtpTransceiver?) {
                            Log.d("RTCClient", "onTrack: $transceiver")
                            transceiver?.receiver?.track()?.let { track ->
                                if (track.kind() == MediaStreamTrack.VIDEO_TRACK_KIND) {
                                    val videoTrack = track as VideoTrack
                                    videoTrack.addSink(surfaceViewRenderer)
                                } else if (track.kind() == MediaStreamTrack.AUDIO_TRACK_KIND) {
                                    val audioTrack = track as AudioTrack
                                    audioTrack.setEnabled(true)
                                } else {

                                }
                            }
                        }

                        override fun onAddTrack(
                            receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?
                        ) {
                            Log.d("RTCClient", "onAddTrack: $receiver")
                        }


                        override fun onRemoveStream(p0: MediaStream?) {
                            Log.d("RTCClient", "onRemoveStream: $p0")
                        }

                        override fun onDataChannel(p0: DataChannel?) {
                            Log.d("RTCClient", "onDataChannel: $p0")
                        }

                        override fun onRenegotiationNeeded() {
                            Log.d("RTCClient", "onRenegotiationNeeded")
                        }
                    })!!

                    peerConnection.createOffer(object : SdpObserver {
                        override fun onCreateSuccess(sdp: SessionDescription?) {
                            peerConnection.setLocalDescription(object : SdpObserver {
                                override fun onSetSuccess() {
                                    Log.d(
                                        "SDP",
                                        peerConnection.localDescription?.description.toString()
                                    )
                                    socketRepository.sendMessage("call", id, JSONObject().apply {
                                        put("type", "offer")
                                        put("to", data.getString("from"))
                                        put("from", socketRepository.getID())
                                        put("sdp", sdp?.description)
                                        put("id", application.user._id)
                                    })
                                }

                                override fun onSetFailure(error: String?) {
                                    Log.e("SDP", "Failed to set local description: $error")
                                }

                                override fun onCreateSuccess(sdp: SessionDescription?) {}
                                override fun onCreateFailure(error: String?) {}
                            }, sdp)
                        }

                        override fun onCreateFailure(error: String?) {
                            Log.e("SDP", "Failed to create offer: $error")
                        }

                        override fun onSetSuccess() {}
                        override fun onSetFailure(error: String?) {
                            Log.d("RTCClient", "onSetFailure: $error")
                        }
                    }, MediaConstraints())

                    peerConnection.addTrack(localVideoTrack)
                    peerConnection.addTrack(localAudioTrack)

                    val user = UserUiState(
                        id = data.getString("id"),
                        surfaceViewRenderer = surfaceViewRenderer,
                        peerConnection = peerConnection,
                        socketId = data.getString("from")
                    )

                    _uiState.value = _uiState.value.copy(
                        users = _uiState.value.users + user
                    )
                }

                "offer" -> {
                    val surfaceViewRenderer = SurfaceViewRenderer(application)
                    initializeSurfaceView(surfaceViewRenderer)

                    val peerConnection = createPeerConnection(object : PeerConnection.Observer {
                        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
                            Log.d("RTCClient", "onSignalingChange: $p0")
                        }

                        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                            Log.d("RTCClient", "onIceConnectionChange: $p0")
                        }

                        override fun onIceConnectionReceivingChange(p0: Boolean) {
                            Log.d("RTCClient", "onIceConnectionReceivingChange: $p0")
                        }

                        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                            Log.d("RTCClient", "onIceGatheringChange: $p0")
                        }

                        override fun onIceCandidate(p0: IceCandidate?) {
                            Log.d("RTCClient", "onIceCandidate: $p0")
                            socketRepository.sendMessage("call", id, JSONObject().apply {
                                put("type", "candidate")
                                put("from", socketRepository.getID())
                                put("to", data.getString("from"))
                                put("candidate", JSONObject().apply {
                                    put("candidate", p0?.sdp)
                                    put("sdpMid", p0?.sdpMid)
                                    put("sdpMLineIndex", p0?.sdpMLineIndex)
                                })
                            })
                        }

                        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
                            Log.d("RTCClient", "onIceCandidatesRemoved: $p0")
                        }

                        override fun onAddStream(mediaStream: MediaStream?) {
                            Log.d("RTCClient", "onAddStream: $mediaStream")
                            mediaStream?.videoTracks?.get(0)?.addSink(surfaceViewRenderer)
                            mediaStream?.audioTracks?.get(0)?.setEnabled(true)
                        }

                        override fun onRemoveStream(p0: MediaStream?) {
                            Log.d("RTCClient", "onRemoveStream: $p0")
                        }

                        override fun onDataChannel(p0: DataChannel?) {
                            Log.d("RTCClient", "onDataChannel: $p0")
                        }

                        override fun onTrack(transceiver: RtpTransceiver?) {
                            Log.d("RTCClient", "onTrack: $transceiver")
                            transceiver?.receiver?.track()?.let { track ->
                                if (track.kind() == MediaStreamTrack.VIDEO_TRACK_KIND) {
                                    val videoTrack = track as VideoTrack
                                    videoTrack.addSink(surfaceViewRenderer)
                                } else if (track.kind() == MediaStreamTrack.AUDIO_TRACK_KIND) {
                                    val audioTrack = track as AudioTrack
                                    audioTrack.setEnabled(true)
                                } else {

                                }
                            }
                        }

                        override fun onRenegotiationNeeded() {
                            Log.d("RTCClient", "onRenegotiationNeeded")
                        }
                    })!!

                    peerConnection.setRemoteDescription(
                        object : SdpObserver {
                            override fun onCreateSuccess(p0: SessionDescription?) {
                                Log.d("RTCClient", "onCreateSuccess: $p0")
                            }

                            override fun onSetSuccess() {
                                peerConnection.createAnswer(object : SdpObserver {
                                    override fun onCreateSuccess(sdp: SessionDescription?) {
                                        peerConnection.setLocalDescription(object : SdpObserver {
                                            override fun onSetSuccess() {
                                                Log.d(
                                                    "SDP",
                                                    peerConnection.localDescription?.description.toString()
                                                )
                                                socketRepository.sendMessage("call",
                                                    id,
                                                    JSONObject().apply {
                                                        put("type", "answer")
                                                        put("to", data.getString("from"))
                                                        put("sdp", sdp?.description)
                                                        put("from", socketRepository.getID())
                                                    })
                                            }

                                            override fun onSetFailure(error: String?) {
                                                Log.e(
                                                    "SDP", "Failed to set local description: $error"
                                                )
                                            }

                                            override fun onCreateSuccess(sdp: SessionDescription?) {}
                                            override fun onCreateFailure(error: String?) {}
                                        }, sdp)
                                    }

                                    override fun onCreateFailure(error: String?) {
                                        Log.e("SDP", "Failed to create answer: $error")
                                    }

                                    override fun onSetSuccess() {}
                                    override fun onSetFailure(error: String?) {

                                    }
                                }, MediaConstraints())

                            }

                            override fun onCreateFailure(p0: String?) {
                                Log.d("RTCClient", "onCreateFailure: $p0")
                            }

                            override fun onSetFailure(p0: String?) {
                                Log.d("RTCClient", "onSetFailure: $p0")
                            }
                        }, SessionDescription(SessionDescription.Type.OFFER, data.getString("sdp"))
                    )

                    peerConnection.addTrack(localVideoTrack)
                    peerConnection.addTrack(localAudioTrack)

                    val user = UserUiState(
                        id = data.getString("id"),
                        surfaceViewRenderer = surfaceViewRenderer,
                        peerConnection = peerConnection,
                        socketId = data.getString("from")
                    )

                    _uiState.value = _uiState.value.copy(
                        users = _uiState.value.users + user
                    )
                }

                "answer" -> {
                    val user = _uiState.value.users.find { it.socketId == data.getString("from") }
                    user?.peerConnection?.setRemoteDescription(
                        object : SdpObserver {
                            override fun onCreateSuccess(p0: SessionDescription?) {
                                Log.d("RTCClient", "onCreateSuccess: $p0")
                            }

                            override fun onSetSuccess() {
                                Log.d("RTCClient", "onSetSuccess")
                            }

                            override fun onCreateFailure(p0: String?) {
                                Log.d("RTCClient", "onCreateFailure: $p0")
                            }

                            override fun onSetFailure(p0: String?) {
                                Log.d("RTCClient", "onSetFailure: $p0")
                            }
                        }, SessionDescription(SessionDescription.Type.ANSWER, data.getString("sdp"))
                    )
                }

                "candidate" -> {
                    val jsonObject = JSONObject(data.getString("candidate"))

                    val user = _uiState.value.users.find { it.socketId == data.getString("from") }
                    user?.peerConnection?.addIceCandidate(
                        IceCandidate(
                            jsonObject.getString("sdpMid"),
                            jsonObject.getInt("sdpMLineIndex"),
                            jsonObject.getString("candidate")
                        )
                    )
                }

                "leave" -> {
                    val user = _uiState.value.users.find { it.socketId == data.getString("from") }
                    user?.peerConnection?.close()
                    _uiState.value = _uiState.value.copy(users = _uiState.value.users.filter {
                        it.id != data.getString("id")
                    })
                }
            }
        }
        initializeSurfaceView(_uiState.value.mainSurfaceViewRenderer)
        startLocalVideo(_uiState.value.mainSurfaceViewRenderer)
    }

    private val eglContext = EglBase.create()
    private val peerConnectionFactory: PeerConnectionFactory by lazy { createPeerConnectionFactory() }
    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun.l.google.com:5349").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:3478").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:5349").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun2.l.google.com:5349").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun3.l.google.com:3478").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun3.l.google.com:5349").createIceServer(),
    )

    private val localVideoSource: VideoSource by lazy {
        peerConnectionFactory.createVideoSource(
            false
        )
    }
    private val localAudioSource: AudioSource by lazy {
        peerConnectionFactory.createAudioSource(
            MediaConstraints()
        )
    }
    private var videoCapturer: CameraVideoCapturer? = null
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null

    private fun createPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        return peerConnectionFactory.createPeerConnection(iceServers, observer)
    }

    private fun initPeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(application)
            .setEnableInternalTracer(true).createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory.builder().setVideoEncoderFactory(
            DefaultVideoEncoderFactory(
                eglContext.eglBaseContext, true, true
            )
        ).setVideoDecoderFactory(DefaultVideoDecoderFactory(eglContext.eglBaseContext))
            .createPeerConnectionFactory()
    }

    private fun initializeSurfaceView(surface: SurfaceViewRenderer) {
        Handler(Looper.getMainLooper()).post {
            surface.run {
                setEnableHardwareScaler(true)
                setMirror(true)
                init(eglContext.eglBaseContext, null)
            }
        }
    }

    private fun startLocalVideo(surface: SurfaceViewRenderer) {
        try {
            val surfaceTextureHelper =
                SurfaceTextureHelper.create("CaptureThread", eglContext.eglBaseContext)
            videoCapturer = getVideoCapturer()
            videoCapturer?.initialize(
                surfaceTextureHelper, surface.context, localVideoSource.capturerObserver
            )
            videoCapturer?.startCapture(1280, 720, 30)
            localVideoTrack =
                peerConnectionFactory.createVideoTrack("local_video_track", localVideoSource)
            localVideoTrack?.addSink(surface)
            localAudioTrack =
                peerConnectionFactory.createAudioTrack("local_audio_track", localAudioSource)
        } catch (e: Exception) {
            Log.e("RTCClient", "Error starting local video", e)
        }
    }

    private fun getVideoCapturer(): CameraVideoCapturer {
        return Camera2Enumerator(application).run {
            deviceNames.find { isFrontFacing(it) }?.let { createCapturer(it, null) }
                ?: throw IllegalStateException("No front-facing camera found.")
        }
    }

    fun switchCamera() {
        videoCapturer?.switchCamera(null)
    }

    fun toggleAudio(mute: Boolean) {
        localAudioTrack?.setEnabled(!mute)
    }

    fun toggleCamera(cameraPause: Boolean) {
        localVideoTrack?.setEnabled(!cameraPause)
    }

    fun endCall(id: String) {
        socketRepository.sendMessage("call", id, JSONObject().apply {
            put("type", "leave")
            put("from", socketRepository.getID())
        })
        socketRepository.sendMessage("leave", "call", id)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FacebookApplication)
                val userRepository = application.container.userRepository
                val socketRepository = application.container.socketRepository

                VideoCallViewModel(
                    application = application,
                    userRepository = userRepository,
                    socketRepository = socketRepository
                )
            }
        }
    }
}