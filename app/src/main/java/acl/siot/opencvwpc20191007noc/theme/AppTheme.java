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

import android.support.annotation.ColorInt;

public class AppTheme {

    @ColorInt
    private int mAccentColor;

    @ColorInt
    private int mBaseColor;

    public AppTheme(@ColorInt int accentColor, @ColorInt int baseColor) {
        mAccentColor = accentColor;
        mBaseColor = baseColor;
    }

    @ColorInt
    public int getAccentColor() {
        return mAccentColor;
    }

    @ColorInt
    public int getBaseColor() {
        return mBaseColor;
    }
}
