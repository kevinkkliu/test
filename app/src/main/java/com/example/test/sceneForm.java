package com.example.test;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import androidx.appcompat.app.AppCompatActivity;

public class sceneForm extends AppCompatActivity {
    private static final String TAG = sceneForm.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    public ModelRenderable andyRenderable;




    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_ux);//activity change
        Button btnHome = (Button)findViewById(R.id.backtodetec);//activity change
        btnHome.setOnClickListener(btnHomeListener);//activity change

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {

                    if (andyRenderable == null) {
                        return;
                    }
                    Anchor anchor = hitResult.createAnchor();
                    // Create the Anchor.

                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());
                    Toast toast = Toast.makeText(this, "create anchor", Toast.LENGTH_LONG);
                    toast.show();
                    // Create the transformable andy and add it to the anchor.
                    TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
                    andy.setParent(anchorNode);
                    andy.setRenderable(andyRenderable);
                    andy.select();
                    Toast toast1 = Toast.makeText(this, "successful", Toast.LENGTH_LONG);
                    toast1.show();

                    ModelRenderable.builder()
                            .setSource(this, Uri.parse("model.sfb"))
                            .build()
                            .thenAccept(modelrenderable -> addModelToScene(anchor,modelrenderable))
                            .exceptionally(
                                    throwable -> {
                                        Toast toast3 =
                                                Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                                        toast3.setGravity(Gravity.CENTER, 0, 0);
                                        toast3.show();
                                        return null;
                                    });
                });
    }

    private void addModelToScene(Anchor anchor, ModelRenderable modelrenderable) {
        AnchorNode anchorNode= new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelrenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.select();

    }

    private Button.OnClickListener btnHomeListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }
}
