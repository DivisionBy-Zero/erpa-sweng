package ch.epfl.sweng.erpa.activities.misc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.util.AttributeSet;

@SuppressWarnings("unused")  // Used by the activity_user_profile.xml
public class FixedAppBarLayoutBehavior extends AppBarLayout.Behavior {
    public FixedAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDragCallback(new DragCallback() {
            @Override public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return false;
            }
        });
    }
}
