package com.nivethsaran.arcoffeemug

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.CompletableFuture

class MainActivity : AppCompatActivity() {


    private lateinit var arFragment: ArFragment
    val viewNodes = mutableListOf<Node>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        arFragment= fragment as ArFragment

        arFragment.setOnTapArPlaneListener{hitResult,x,y ->
            loadModel{modelRenderable, viewRenderable ->
                addNodeToScene(hitResult.createAnchor(),modelRenderable,viewRenderable)
            }
        }

    }


    private fun addNodeToScene(
        anchor: Anchor,
        modelRenderable: ModelRenderable,
        viewRenderable: ViewRenderable
    ) {
        val anchorNode = AnchorNode(anchor)
        val modelNode = TransformableNode(arFragment.transformationSystem).apply {
            renderable = modelRenderable
            setParent(anchorNode)
            arFragment.arSceneView.scene.addChild(anchorNode)
            select()
        }
        val viewNode = Node().apply {
            renderable = null
            setParent(modelNode)
            val box = modelNode.renderable?.collisionShape as Box
            localPosition = Vector3(0f, box.size.y, 0f)
            (viewRenderable.view as Button).setOnClickListener {
                arFragment.arSceneView.scene.removeChild(anchorNode)
                viewNodes.remove(this)
            }
        }
        viewNodes.add(viewNode)
        modelNode.setOnTapListener { _, _ ->
            if(!modelNode.isTransforming) {
                if(viewNode.renderable == null) {
                    viewNode.renderable = viewRenderable
                } else {
                    viewNode.renderable = null
                }
            }
        }
    }

    private fun createDeleteButton(): Button {
        return Button(this).apply {
            text = "REMOVE"
            setBackgroundColor(Color.CYAN)
            setTextColor(Color.WHITE)
        }
    }


    private fun loadModel(callback: (ModelRenderable, ViewRenderable) -> Unit) {
        val modelRenderable = ModelRenderable.builder()
            .setSource(this, R.raw.coffeecup)
            .build()

        val viewRenderable = ViewRenderable.builder()
            .setView(this, createDeleteButton())
            .build()

        CompletableFuture.allOf(modelRenderable, viewRenderable)
            .thenAccept {
                callback(modelRenderable.get(), viewRenderable.get())
            }
            .exceptionally {
                Toast.makeText(this, "Error loading model: $it", Toast.LENGTH_LONG).show()
                null
            }
    }



}