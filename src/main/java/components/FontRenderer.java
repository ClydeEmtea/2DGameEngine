package components;

import engine.Component;
import org.joml.Vector4f;


public class FontRenderer extends Component {

    private String text = "Hello Editor";
    private Vector4f color = new Vector4f(1, 1, 1, 1);

    public void setText(String text) {
        this.text = text;
    }

    public void setColor(Vector4f color) {
        this.color.set(color);
    }

    @Override
    public void update(float dt) {
    }

    // TODO: Implement font rendering


}
