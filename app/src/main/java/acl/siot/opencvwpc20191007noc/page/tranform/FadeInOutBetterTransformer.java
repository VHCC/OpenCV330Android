package acl.siot.opencvwpc20191007noc.page.tranform;

import android.view.View;

/**
 * Created by IChen.Chu on 2020/05/25
 */
public class FadeInOutBetterTransformer extends ABaseTransformer{
    @Override
    protected void onTransform(View view, float position) {
        if(position <= -1.0F || position >= 1.0F) {
            view.setTranslationX(view.getWidth() * position);
            view.setAlpha(0.0F);
        } else if( position == 0.0F ) {
            view.setTranslationX(view.getWidth() * position);
            view.setAlpha(1.0F);
        } else {
            // position is between -1.0F & 0.0F OR 0.0F & 1.0F
            view.setTranslationX(view.getWidth() * -position);
//            view.setTranslationY(view.getHeight() * -position);
            view.setAlpha(1.0F - Math.abs(position));
//            view.setAlpha(1.0F);
        }
    }
}
