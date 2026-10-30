package tmb.randy.tmbgriefergames.core.flow;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType;
import tmb.randy.tmbgriefergames.core.flow.node.NodeTypes;
import tmb.randy.tmbgriefergames.core.flow.node.Parameter;

/** One block of a flow: its type, position on the editor canvas and parameter values. */
public class FlowNode {
    private String id;
    private String type;
    private float x;
    private float y;
    private Map<String, String> params = new HashMap<>();

    public FlowNode(NodeType type, float x, float y) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.type = type.id();
        this.x = x;
        this.y = y;
        for (Parameter parameter : type.parameters())
            params.put(parameter.key(), parameter.defaultValue());
    }

    private FlowNode(FlowNode other) {
        this.id = other.id;
        this.type = other.type;
        this.x = other.x;
        this.y = other.y;
        this.params = new HashMap<>(other.params);
    }

    /** Copy with the same id, so connections stay valid. */
    FlowNode copy() {
        return new FlowNode(this);
    }

    public String getId() {
        return id;
    }

    /** The node type, null if the file references a type that does not exist (anymore). */
    public NodeType getType() {
        return NodeTypes.get(type);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public String getParam(String key) {
        return params.getOrDefault(key, "");
    }

    public void setParam(String key, String value) {
        params.put(key, value);
    }

    void ensureDefaults() {
        if (params == null)
            params = new HashMap<>();
    }
}
