package com.example.facebook.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verifyOrder
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class VoiceRecorderTest {

    private lateinit var recorder: MediaRecorder
    private lateinit var context: Context

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockkConstructor(MediaRecorder::class)
        mockkStatic(ContextCompat::class)

        context = mockk(relaxed = true)

        recorder = mockk {
            every { setAudioSource(any()) } just Runs
            every { setOutputFormat(any()) } just Runs
            every { setAudioEncoder(any()) } just Runs
            every { setOutputFile(any<String>()) } just Runs
            every { prepare() } just Runs
            every { start() } just Runs
            every { stop() } just Runs
            every { release() } just Runs
            every { pause() } just Runs
            every { resume() } just Runs
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `test permission granted`() {
        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
        } returns PackageManager.PERMISSION_GRANTED

        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        assertTrue(permissionGranted)
    }

    @Test
    fun `test permission denied`() {
        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
        } returns PackageManager.PERMISSION_DENIED

        val permissionDenied = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_DENIED

        assertTrue(permissionDenied)
    }

    @Test
    fun `test start recorder`() = runTest {
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("audio.mp3")
            prepare()
            start()
        }

        verifyOrder {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setOutputFile("audio.mp3")
            recorder.prepare()
            recorder.start()
        }
    }

    @Test
    fun `test stop recorder`() = runTest {
        recorder.apply {
            stop()
            release()
        }

        verifyOrder {
            recorder.stop()
            recorder.release()
        }
    }

    @Test
    fun `test recorder pause and resume`() = runTest {
        recorder.apply {
            pause()
            resume()
        }

        verifyOrder {
            recorder.pause()
            recorder.resume()
        }
    }
}
