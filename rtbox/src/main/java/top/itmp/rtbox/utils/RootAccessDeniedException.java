package top.itmp.rtbox.utils;

import java.io.IOException;

/**
 * Created by hz on 2016/5/4.
 */
public class RootAccessDeniedException extends IOException {
    public RootAccessDeniedException() {
        super();
    }

    public RootAccessDeniedException(String detailMessage) {
        super(detailMessage);
    }
}
