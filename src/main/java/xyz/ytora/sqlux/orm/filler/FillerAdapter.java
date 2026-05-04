package xyz.ytora.sqlux.orm.filler;

/**
 * 自动填充适配器
 *
 * @author ytora
 * @since 1.0
 */
public class FillerAdapter implements IFiller {
    @Override
    public Object onInsert() {
        return null;
    }

    @Override
    public Object onUpdate() {
        return null;
    }

    @Override
    public boolean overwriteOnUpdate() {
        return false;
    }
}
