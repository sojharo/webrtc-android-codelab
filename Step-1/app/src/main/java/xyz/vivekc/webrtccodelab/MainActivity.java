package xyz.vivekc.webrtccodelab;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize PeerConnectionFactory globals.
        //Params are context, initAudio,initVideo and videoCodecHwAcceleration
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);

        //Create a new PeerConnectionFactory instance.
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        PeerConnectionFactory peerConnectionFactory = new PeerConnectionFactory(options);


        //Now create a VideoCapturer instance. Callback methods are there if you want to do something! Duh!
        VideoCapturer videoCapturerAndroid = getVideoCapturer(new CameraVideoCapturer.CameraEventsHandler() {
            @Override
            public void onCameraError(String s) {
                Log.d("onCameraError", s);
            }

            @Override
            public void onCameraFreezed(String s) {
                Log.d("onCameraFreezed", s);
            }

            @Override
            public void onCameraOpening(int i) {
                Log.d("onCameraOpening", i + "");
            }

            @Override
            public void onFirstFrameAvailable() {
                Log.d("onFirstFrameAvailable", "Hola");
            }

            @Override
            public void onCameraClosed() {
                Log.d("onCameraClosed", ":(");
            }
        });

        //Create MediaConstraints - Will be useful for specifying video and audio constraints. More on this later!
        MediaConstraints constraints = new MediaConstraints();

        //Create a VideoSource instance
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid, constraints);
        VideoTrack localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);

        //create an AudioSource instance
        AudioSource audioSource = peerConnectionFactory.createAudioSource(constraints);
        AudioTrack localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);


        //We use the VideoRendererGui for displaying video from Cam.
        //Get the surfaceview instance.
        GLSurfaceView videoView = (GLSurfaceView) findViewById(R.id.gl_surface_view);

        // Then we set that view, and pass a Runnable to run once the surface is ready
        VideoRendererGui.setView(videoView, new Runnable() {
            @Override
            public void run() {
                Log.d("run", "VideoRendererGUI callback");
            }
        });

        // Now that VideoRendererGui is ready, we can get our VideoRenderer
        VideoRenderer renderer = null;
        try {
            renderer = VideoRendererGui.createGui(0, 0, 100,100, RendererCommon.ScalingType.SCALE_ASPECT_FIT,true);
            // And finally, with our VideoRenderer ready, we
            // can add our renderer to the VideoTrack.
            localVideoTrack.addRenderer(renderer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Cycle through likely device names for the camera and return the first
    // capturer that works, or crash if none do.// TODO: 16/10/16 don't crash the app. Not good.
    private VideoCapturer getVideoCapturer(CameraVideoCapturer.CameraEventsHandler eventsHandler) {
        String[] cameraFacing = { "front", "back" };
        int[] cameraIndex = { 0, 1 };
        int[] cameraOrientation = { 0, 90, 180, 270 };
        for (String facing : cameraFacing) {
            for (int index : cameraIndex) {
                for (int orientation : cameraOrientation) {
                    String name = "Camera " + index + ", Facing " + facing +
                            ", Orientation " + orientation;
                    VideoCapturer capturer = VideoCapturerAndroid.create(name,eventsHandler);
                    if (capturer != null) {
                        Log.d("Using camera: " ,name);
                        return capturer;
                    }
                }
            }
        }
        throw new RuntimeException("Failed to open capturer");
    }
}