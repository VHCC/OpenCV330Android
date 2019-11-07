/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package acl.siot.opencvwpc20191007noc.theme;


import android.support.annotation.NonNull;

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.util.MLog;
import de.greenrobot.event.EventBus;


public class AppThemeManager implements AppThemeMethods {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private static AppThemeManager sInstance;

    public static synchronized void init(@NonNull AppTheme theme) {
        if (null == sInstance) {
            sInstance = new AppThemeManager(theme);
        }
    }

    public static synchronized AppThemeManager getInstance() {
        if (null == sInstance) {
            throw new RuntimeException("Cannot obtain AppThemeManager before calling init().");
        }

        return sInstance;
    }

    private AppTheme mTheme;

    public AppThemeManager(@NonNull AppTheme theme) {
        setTheme(theme);
    }

    public AppTheme getTheme() {
        return mTheme;
    }

    @Override
    public void setTheme(@NonNull AppTheme theme) {
        mTheme = theme;
        AppBus.getInstance().postSticky(theme);
    }

}
