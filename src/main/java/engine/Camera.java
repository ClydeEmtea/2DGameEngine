package engine;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {

    private Matrix4f projectionMatrix, viewMatrix;


    private float zoom = 1.0f;
    public Vector2f position;

    public Camera(Vector2f position) {
        this.position = position;
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        adjustProjection();
    }

    public void adjustProjection() {
        projectionMatrix.identity();
        float left   = 0.0f;
        float right  = 32.0f * 40.0f / zoom;
        float bottom = 0.0f;
        float top    = 32.0f * 21.0f / zoom;
        projectionMatrix.ortho(left, right, bottom, top, 0.0f, 100.0f);
    }

    public Matrix4f getViewMatrix() {
        Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);
        this.viewMatrix.identity();
        viewMatrix.lookAt(new Vector3f(position.x, position.y, 20.0f), cameraFront.add(position.x, position.y, 0.0f), cameraUp);
        return this.viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = Math.max(zoom, 0.5f);
        this.zoom = Math.min(this.zoom, 2.0f);
        adjustProjection();
    }
}
