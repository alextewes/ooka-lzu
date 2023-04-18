package lzu;

public class ComponentInstance {
    private int ID;
    private String name;
    private ComponentState state;
    private Class<?> startClass;

    public ComponentInstance(int ID, String name, Class<?> startClass) {
        this.ID = ID;
        this.name = name;
        this.startClass = startClass;
        this.state = ComponentState.INITIALIZED;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ComponentState getState() {
        return state;
    }

    public void setState(ComponentState state) {
        this.state = state;
    }

    public Class<?> getStartClass() {
        return startClass;
    }

    public void setStartClass(Class<?> startClass) {
        this.startClass = startClass;
    }
}

