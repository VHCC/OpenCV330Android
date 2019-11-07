package acl.siot.opencvwpc20191007noc.page.tranform;

import android.view.View;

/**
 * Created by IChen.Chu on 2018/9/26
 */
public class ScaleInOutTransformer extends ABaseTransformer{
    @Override
    protected void onTransform(View view, float position) {
        view.setPivotX(position < 0 ? 0 : view.getWidth());
        view.setPivotY(view.getHeight() / 2f);
        float scale = position < 0 ? 1f + position : 1f - position;
        view.setScaleX(scale);
        view.setScaleY(scale);
    }
}
