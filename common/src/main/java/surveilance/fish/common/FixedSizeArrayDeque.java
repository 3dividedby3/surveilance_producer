package surveilance.fish.common;

import java.util.ArrayDeque;

public class FixedSizeArrayDeque<T> extends ArrayDeque<T> {

    private static final long serialVersionUID = -8782050853107851941L;

    private final int fixedSize;
    
    public FixedSizeArrayDeque(int fixedSize) {
        this.fixedSize = fixedSize;
    }
    
    @Override
    public boolean add(T elem) {
        if (size() >= fixedSize) {
            pollFirst();
        }
        return super.add(elem);
    }
}
