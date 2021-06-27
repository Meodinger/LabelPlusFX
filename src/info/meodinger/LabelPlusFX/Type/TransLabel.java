package info.meodinger.LabelPlusFX.Type;

/**
 * @author Meodinger
 * Date: 2021/5/24
 * Location: info.meodinger.LabelPlusFX.Type
 */
public class TransLabel {

    private int index;
    private double x;
    private double y;
    private int groupId;
    private String text;

    // For jackson
    public TransLabel() {}

    public TransLabel(int index, double x, double y, int groupId, String text) {
        this.index = index;
        this.x = x;
        this.y = y;
        this.groupId = groupId;
        this.text = text;
    }

    public int getIndex() {
        return index;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getText() {
        return text;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public void setText(String text) {
        this.text = text;
    }
}
