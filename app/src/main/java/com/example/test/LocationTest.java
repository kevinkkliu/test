package com.example.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.google.ar.core.Anchor;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import com.google.ar.sceneform.ArSceneView;

import uk.co.appoly.arcorelocation.LocationMarker;
import uk.co.appoly.arcorelocation.LocationScene;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;


public class LocationTest extends AppCompatActivity {
    private LocationScene locationScene;
    public ModelRenderable andyRenderable;
    private ArFragment arFragment;
    public ArSceneView arSceneView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ux);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);


        CompletableFuture<ModelRenderable> model = ModelRenderable.builder()
                .setSource(this, R.raw.model)
                .build();


        CompletableFuture.allOf(model)
                .handle(
                        (notUsed, throwable) ->
                        {
                            if (throwable != null) {
                                Toast.makeText(this,"0.0",Toast.LENGTH_LONG);
                                return null;
                            }

                            try {
                                andyRenderable = model.get();

                            } catch (InterruptedException | ExecutionException ex) {
                                Toast.makeText(this,"Unable to load render",Toast.LENGTH_LONG);
                            }
                            return null;
                        });


                arSceneView
                .getScene()
                .setOnTouchListener(
                        frameTime -> {

                            if (locationScene == null) {
                                locationScene = new LocationScene(this, arSceneView);
                                locationScene.mLocationMarkers.add(
                                        new LocationMarker(
                                                -0.119677,
                                                51.478494,
                                                getAndy()));
                            }



                            if (locationScene != null) {
                                locationScene.processFrame(frame);
                            }

                        });
    }
    private Node getAndy() {
        Node base = new Node();
        base.setRenderable(andyRenderable);
        Context c = this;
        base.setOnTapListener((v, event) -> {
            Toast.makeText(
                    c, "Andy touched.", Toast.LENGTH_LONG)
                    .show();
        });
        return base;
    }

    private void addModelToScene(Anchor anchor, ModelRenderable modelrenderable) {
        AnchorNode anchorNode= new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelrenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.select();

    }
}