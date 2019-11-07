package acl.siot.opencvwpc20191007noc.page.subPage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.util.MLog;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by IChen.Chu on 2018/9/26
 */
public class SubPageEmptyFragment extends Fragment {

    private static final MLog mLog = new MLog(false);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SubPagesMainFragment.
     */
    public static SubPageEmptyFragment newInstance() {
        SubPageEmptyFragment fragment = new SubPageEmptyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    // Constructor
    public SubPageEmptyFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mLog.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sub_empty, container, false);
        return rootView;
    }
}
