package acl.siot.opencvwpc20191007noc.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

import acl.siot.opencvwpc20191007noc.R;


/**
 * Created by TsungMu on 2016/10/11.
 */
public class MessageTools {

    private static Queue<Toast> toastQueue = new Queue<Toast>() {

        private ArrayList<Toast> toasts = new ArrayList<>();

        @Override
        public boolean add(Toast toast) {
            return toasts.add(toast);
        }

        @Override
        public boolean offer(Toast toast) {
            return false;
        }

        @Override
        public Toast remove() {
            return null;
        }

        @Override
        public Toast poll() {
            return null;
        }

        @Override
        public Toast element() {
            return null;
        }

        @Override
        public Toast peek() {
            return toasts.get(0);
        }

        @Override
        public int size() {
            return toasts.size();
        }

        @Override
        public boolean isEmpty() {
            return toasts.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return toasts.contains(o);
        }

        @NonNull
        @Override
        public Iterator<Toast> iterator() {
            return null;
        }

        @NonNull
        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @NonNull
        @Override
        public <T> T[] toArray(@NonNull T[] ts) {
            return null;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(@NonNull Collection<?> collection) {
            return false;
        }

        @Override
        public boolean addAll(@NonNull Collection<? extends Toast> collection) {
            return false;
        }

        @Override
        public boolean removeAll(@NonNull Collection<?> collection) {
            return false;
        }

        @Override
        public boolean retainAll(@NonNull Collection<?> collection) {
            return false;
        }

        @Override
        public void clear() {
            toasts.clear();
        }
    };

    public static void showToast(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        setToastStyle(context, toast);
        if (toastQueue.size() > 0) {
            toastQueue.peek().cancel();
        }
        toastQueue.clear();
        toastQueue.add(toast);
        toastQueue.peek().show();
    }

    public static void showLongToast(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        setToastStyle(context, toast);
        if (toastQueue.size() > 0) {
            toastQueue.peek().cancel();
        }
        toastQueue.clear();
        toastQueue.add(toast);
        toastQueue.peek().show();
    }

    private static Toast setToastStyle(Context context, @NonNull Toast toast) {
//        toast.setGravity(Gravity.CENTER, 0,0);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
//        toast.setView(inflater.inflate(R.layout.toast_custom_view, null));
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
//        TextView v = (TextView) toast.getView().findViewById(R.id.customToastText);
        int toastPadding = (int) context.getResources().getDimension(R.dimen.message_tool_toast_padding);
//        v.setTextColor(Color.BLACK);
//        v.setBackgroundColor(Color.RED);
//        v.setPadding(toastPadding, toastPadding, toastPadding, toastPadding);
//        toast.getView().setBackgroundColor(Color.BLUE);
        return toast;
    }
}
