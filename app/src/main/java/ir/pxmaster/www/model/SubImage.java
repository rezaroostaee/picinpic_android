package ir.pxmaster.www.model;

import android.net.Uri;

public class SubImage {
    Uri uri;
    boolean selected;

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
