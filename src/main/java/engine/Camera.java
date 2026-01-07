package engine;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import util.Constants;

public class Camera {

    private Matrix4f projectionMatrix, viewMatrix, inverseProjection, inverseView;


    private float zoom = 2.0f;
    public Vector2f position;
    private Vector2f projectionSize = new Vector2f(6,3);

    public Camera(Vector2f position) {
        this.position = position;
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        this.inverseProjection = new Matrix4f();
        this.inverseView = new Matrix4f();
        adjustProjection();
    }

    public void adjustProjection() {
        projectionMatrix.identity();
        projectionMatrix.ortho(0.0f, projectionSize.x * this.zoom,
                0.0f, projectionSize.y * zoom, 0.0f, 100.0f);
        projectionMatrix.invert(inverseProjection);
    }

    public Matrix4f getViewMatrix() {
        Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);
        this.viewMatrix.identity();
        viewMatrix.lookAt(new Vector3f(position.x, position.y, 20.0f),
                cameraFront.add(position.x, position.y, 0.0f),
                cameraUp);
        this.viewMatrix.invert(inverseView);

        return this.viewMatrix;
    }


    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public Matrix4f getInverseProjection() {
        return this.inverseProjection;
    }

    public Matrix4f getInverseView() {
        return this.inverseView;
    }

    public Vector2f getProjectionSize() {
        return this.projectionSize;
    }


    public void addZoom(float value) {
        this.zoom += value;
    }


    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = Math.max(zoom, 0.2f);
        this.zoom = Math.min(this.zoom, 10.0f);
        adjustProjection();
    }

    public Vector2f screenToWorld(float screenX, float screenY) {
        float camHalfWidth = projectionSize.x * 0.5f * zoom;
        float camHalfHeight = projectionSize.y * 0.5f * zoom;

        float worldX = (screenX / Window.get().getWidth()) * (camHalfWidth * 2) + position.x;
        float worldY = ((Window.get().getHeight() - screenY) / Window.get().getHeight()) * (camHalfHeight * 2) + position.y;

        return new Vector2f(worldX, worldY);
    }

    public void lookAt(Vector2f location) {
        float viewWidth = projectionSize.x * zoom;
        float viewHeight = projectionSize.y * zoom;

        this.position.set(
                location.x - viewWidth * 0.5f,
                location.y - viewHeight * 0.5f
        );
    }



}
